/**
 * ★ Insight ─────────────────────────────────────
 * 고객 여정(Customer Journey) 기반 부하테스트.
 *
 * 핵심 패턴: "Correlation"
 * - 이전 API 응답에서 데이터를 추출해 다음 요청에 사용하는 기법
 * - Step 1 검색 결과 → productId 추출 → Step 2 상세 조회에 사용
 * - Step 5 펀딩 목록 → fundingId, wishlistItemId 추출 → Step 6 주문에 사용
 *
 * 실행 모드:
 * - 읽기 전용 모드: TEST_RECEIVER_ID 미설정 → Step 1~5만 실행 (안전)
 * - 전체 여정 모드: TEST_RECEIVER_ID 설정 → Step 6 주문 생성까지 실행
 * ─────────────────────────────────────────────────
 */
import http from 'k6/http';
import {check, group, sleep} from 'k6';
import {SharedArray} from 'k6/data';
import {Rate, Trend} from 'k6/metrics';
import {getTokens} from '../modules/auth.js';
import {
    AUTH0_AUDIENCE,
    AUTH0_CLIENT_ID,
    AUTH0_CLIENT_SECRET,
    AUTH0_DOMAIN,
    authHeaders,
    mockAuthHeaders,
    MOCK_AUTH,
    BASE_URL,
    PRODUCT_IDS,
    randomItem,
    SEARCH_KEYWORDS,
    getRandomWishlistItem,
} from '../modules/config.js';

// -- 커스텀 메트릭 --
// Trend: 각 API별 응답 시간을 개별 추적 (true = 밀리초 단위)
// k6 기본 http_req_duration은 전체 평균이라 API별 SLO 검증이 불가능
const searchDuration = new Trend('search_duration', true);
const productDuration = new Trend('product_duration', true);
const wishlistDuration = new Trend('wishlist_duration', true);
const cartDuration = new Trend('cart_duration', true);
const fundingListDuration = new Trend('funding_list_duration', true);
const orderDuration = new Trend('order_duration', true);
const errorRate = new Rate('error_rate');

// -- 환경변수: 주문 생성 여부를 제어하는 스위치 --
// 0이면 읽기 전용 모드, 양수면 해당 회원 ID를 수령자로 주문 생성
const TEST_RECEIVER_ID = parseInt(__ENV.TEST_RECEIVER_ID || '0');

/**
 * ★ Insight ─────────────────────────────────────
 * SharedArray + open()은 "init context"에서만 호출 가능하다.
 * init context = 스크립트 최상위 레벨 (setup/default/teardown 밖).
 *
 * SharedArray는 모든 VU가 동일 메모리를 공유하므로
 * 100개 계정 × 100 VU여도 메모리 1카피분만 사용.
 * setup()에서 로드하면 각 VU마다 복사본이 생겨 메모리 낭비.
 * ─────────────────────────────────────────────────
 */
const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

// -- 부하 단계 설정 --
// DB 커넥션풀(HikariCP 10개) 한계로 VU 100→60 하향
// 10 VU(워밍업) → 30 VU → 60 VU(피크) → 30 VU(쿨다운) → 0
export const options = {
    setupTimeout: '120s',
    stages: [
        {duration: '30s', target: 10},
        {duration: '60s', target: 30},
        {duration: '120s', target: 60},
        {duration: '30s', target: 30},
        {duration: '15s', target: 0},
    ],
    /**
     * ★ SLO 기반 threshold ─────────────────────────
     * 조회 API: p95 < 200ms (이커머스 보편 기준)
     * 쓰기 API: p95 < 500ms
     * tags.name으로 API별 개별 threshold 적용.
     * k6가 종료 시 threshold 위반 여부를 자동 판정한다.
     * ─────────────────────────────────────────────────
     */
    thresholds: {
        'http_req_duration{name:product_search}': ['p(95)<200'],
        'http_req_duration{name:product_detail}': ['p(95)<200'],
        'http_req_duration{name:wishlist}': ['p(95)<200'],
        'http_req_duration{name:funding_list}': ['p(95)<200'],
        'http_req_duration{name:cart_add}': ['p(95)<500'],
        // 주문 = 펀딩 참여 포함 (DB 트랜잭션 경합) → 1000ms가 현실적
        'http_req_duration{name:order_create}': ['p(95)<1000'],
        'http_req_failed': ['rate<0.01'],
        'error_rate': ['rate<0.05'],
    },
};

/**
 * setup() — 모든 VU 시작 전 단 한 번 실행.
 *
 * Phase 1 (MOCK_AUTH=true): 토큰 발급 없이 즉시 시작. DynamicMockAuthFilter가 헤더로 인증 처리.
 * Phase 2 (MOCK_AUTH=false): Auth0에서 계정별 JWT 토큰을 벌크 발급.
 *
 * 반환값은 각 VU의 default(data) 인자로 전달된다.
 */
export function setup() {
    if (MOCK_AUTH) {
        console.log('Phase 1: Mock Auth 모드. Auth0 토큰 발급을 건너뜁니다.');
        return {tokens: [], mockAuth: true};
    }

    const tokens = getTokens(
        accounts, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    if (tokens.length === 0) {
        throw new Error('토큰 발급 실패. Auth0 환경변수를 확인하세요.');
    }

    return {tokens, mockAuth: false};
}

/**
 * ★ 설계 의도 ─────────────────────────────────────
 * VU별 토큰 할당: __VU % tokens.length
 * - __VU는 1부터 시작하는 VU 고유 번호
 * - 100 VU, 100 토큰이면 1:1 매핑
 * - VU > 토큰 수면 라운드로빈으로 재사용
 *
 * sleep()에 랜덤 오프셋을 더하는 이유:
 * 실제 사용자는 일정한 간격으로 클릭하지 않는다.
 * 0.3 + random(0.5) = 0.3~0.8초 → "사고 시간(think time)" 시뮬레이션
 * ─────────────────────────────────────────────────
 */
export default function (data) {
    // Phase 1: VU 번호를 memberId로 사용하여 Mock 헤더 생성
    // Phase 2: setup()에서 발급한 Auth0 토큰으로 Bearer 헤더 생성
    // Mock Auth: VU 번호를 loadtest member ID에 매핑 (1001~1100)
    // Real Auth: setup()에서 발급한 Auth0 토큰으로 Bearer 헤더 생성
    const opts = data.mockAuth
        ? mockAuthHeaders(1001 + (__VU % 100))
        : authHeaders(data.tokens[__VU % data.tokens.length]);

    // ── Step 1: 상품 검색 ──────────────────────────
    // Correlation: 검색 결과에서 productId를 동적으로 추출
    // check() 실패 시 후속 Step을 스킵하여 무의미한 트래픽 방지
    let productId = randomItem(PRODUCT_IDS); // 검색 실패 시 fallback
    let searchOk = false;
    group('1_product_search', function () {
        const keyword = randomItem(SEARCH_KEYWORDS);
        const res = http.get(
            `${BASE_URL}/api/v2/products/search?keyword=${encodeURIComponent(keyword)}&page=0&size=10`,
            Object.assign({}, opts, {tags: {name: 'product_search'}})
        );
        searchOk = check(res, {'search ok': (r) => r.status === 200});
        searchDuration.add(res.timings.duration);
        errorRate.add(res.status >= 400);

        // 응답에서 productId 추출 — RsData 래퍼: { resultCode, msg, data: { content: [...] } }
        if (res.status === 200) {
            try {
                const body = res.json();
                const content = body.data && body.data.content;
                if (content && content.length > 0) {
                    productId = content[Math.floor(Math.random() * content.length)].id;
                }
            } catch (e) { /* JSON 파싱 실패 시 fallback 사용 */
            }
        }
    });

    // Step 1 실패 시 나머지 Step 스킵 — 서버 장애 상황에서 무의미한 요청 방지
    if (!searchOk) {
        sleep(1 + Math.random() * 2);
        return;
    }

    sleep(0.3 + Math.random() * 0.5); // think time

    // ── Step 2: 상품 상세 ──────────────────────────
    // Step 1에서 추출한 productId로 조회 (correlation 연계)
    group('2_product_detail', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/products/${productId}`,
            Object.assign({}, opts, {tags: {name: 'product_detail'}})
        );
        // 404도 정상 — 검색과 상세 사이에 상품이 삭제될 수 있음
        check(res, {'product ok': (r) => r.status === 200 || r.status === 404});
        productDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500); // 5xx만 에러로 카운트
    });

    sleep(0.3 + Math.random() * 0.5);

    // ── Step 3: 위시리스트 조회 ────────────────────
    group('3_wishlist', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/wishlists/me?page=0&size=10`,
            Object.assign({}, opts, {tags: {name: 'wishlist'}})
        );
        check(res, {'wishlist ok': (r) => r.status === 200});
        wishlistDuration.add(res.timings.duration);
        errorRate.add(res.status >= 400);
    });

    sleep(0.2 + Math.random() * 0.3);

    // ── Step 4: 장바구니 추가 ──────────────────────
    // targetId = wishlistItemId (서버가 wishlistItemId로 해석)
    const wishItem = getRandomWishlistItem();
    group('4_cart_add', function () {
        const body = JSON.stringify({targetId: wishItem.wishlistItemId, amount: 10000});
        const res = http.post(
            `${BASE_URL}/api/v2/carts`,
            body,
            Object.assign({}, opts, {tags: {name: 'cart_add'}})
        );
        // 400 허용 — 이미 장바구니에 있는 아이템 재추가 시 비즈니스 에러 반환
        check(res, {'cart ok': (r) => r.status === 200 || r.status === 201 || r.status === 400});
        cartDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.2 + Math.random() * 0.3);

    // ── Step 5: 펀딩 목록 조회 ────────────────────
    // Correlation: fundingId, wishlistItemId를 추출하여 Step 6 주문에 사용
    let fundingId = null;
    let wishlistItemId = null;
    group('5_funding_list', function () {
        const page = Math.floor(Math.random() * 3); // 0~2 페이지 랜덤 조회
        const res = http.get(
            `${BASE_URL}/api/v2/fundings/list?page=${page}&size=10`,
            Object.assign({}, opts, {tags: {name: 'funding_list'}})
        );
        check(res, {'funding list ok': (r) => r.status === 200});
        fundingListDuration.add(res.timings.duration);
        errorRate.add(res.status >= 400);

        // FundingResponseDto에서 fundingId, wishlistItemId 추출
        // fundingId는 null 체크용으로만 추출. API는 wishlistItemId로 funding을 자동 매칭.
        // receiverId는 DTO에 없으므로 환경변수(TEST_RECEIVER_ID)로 주입
        if (res.status === 200) {
            try {
                const body = res.json();
                const content = body.data && body.data.content;
                if (content && content.length > 0) {
                    const funding = content[Math.floor(Math.random() * content.length)];
                    fundingId = funding.fundingId;
                    wishlistItemId = funding.wishlistItemId;
                }
            } catch (e) { /* correlation 실패 시 주문 스킵 */
            }
        }
    });

    sleep(0.3 + Math.random() * 0.5);

    /**
     * ── Step 6: 주문 생성 ──────────────────────────
     *
     * ★ 조건부 실행 ─────────────────────────────────
     * 3가지 조건이 모두 충족되어야 실행:
     * 1. fundingId가 Step 5에서 추출됨
     * 2. wishlistItemId가 Step 5에서 추출됨
     * 3. TEST_RECEIVER_ID 환경변수가 설정됨 (양수)
     *
     * 결제 방식으로 DEPOSIT(예치금)을 사용하여
     * TossPayments PG 연동을 우회한다.
     * 실제 결제가 발생하지 않으므로 부하테스트에 안전.
     * ─────────────────────────────────────────────────
     */
    if (fundingId && wishlistItemId && TEST_RECEIVER_ID > 0) {
        group('6_order_create', function () {
            const body = JSON.stringify({
                items: [{
                    wishlistItemId: wishlistItemId,
                    receiverId: TEST_RECEIVER_ID,
                    amount: 1000,
                    orderItemType: 'FUNDING_GIFT',
                }],
                method: 'DEPOSIT',
            });
            const res = http.post(
                `${BASE_URL}/api/v2/orders`,
                body,
                Object.assign({}, opts, {tags: {name: 'order_create'}})
            );
            check(res, {'order ok': (r) => r.status === 200 || r.status === 201});
            orderDuration.add(res.timings.duration);
            errorRate.add(res.status >= 500);
        });
    }

    sleep(0.5 + Math.random() * 1.0);
}

// -- Teardown: 테스트 완료 후 단 한 번 실행 --
export function teardown(data) {
    console.log(`테스트 완료. 인증 모드: ${data.mockAuth ? 'Phase 1 (Mock Auth)' : 'Phase 2 (Auth0 JWT)'}`);
    if (!data.mockAuth) {
        console.log(`사용된 토큰: ${data.tokens.length}개`);
    }
    if (TEST_RECEIVER_ID > 0) {
        console.log('주문 생성 모드 (TEST_RECEIVER_ID 설정됨)');
    } else {
        console.log('읽기 전용 모드 (TEST_RECEIVER_ID 미설정). Step 1~5만 실행됨.');
    }
    console.log('DB 정합성 검증이 필요하면 verify.sh를 실행하세요.');
}
