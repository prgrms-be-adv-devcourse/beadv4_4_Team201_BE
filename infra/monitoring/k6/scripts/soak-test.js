/**
 * Soak Test (Endurance Test) — 장시간 안정성 검증.
 *
 * 평균 부하(30 VU)로 30분간 실행하여
 * 시간 경과에 따른 성능 저하를 감지한다.
 *
 * 감지 대상:
 * - 메모리 누수 (JVM heap 점진적 증가)
 * - DB 커넥션풀 고갈 (HikariCP 대기 타임아웃)
 * - 로그 파일 디스크 점유
 * - GC pause 증가
 *
 * k6 공식 권장: 평균 프로덕션 부하로 수 시간 실행.
 * 여기서는 30분 축약 버전. 프로덕션은 4-8시간 권장.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Rate, Trend } from 'k6/metrics';
import { getTokens } from '../modules/auth.js';
import {
    AUTH0_AUDIENCE, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET,
    AUTH0_DOMAIN, authHeaders, mockAuthHeaders, MOCK_AUTH,
    BASE_URL, PRODUCT_IDS, randomItem, SEARCH_KEYWORDS,
    getRandomWishlistItem,
} from '../modules/config.js';

const searchDuration = new Trend('search_duration', true);
const productDuration = new Trend('product_duration', true);
const wishlistDuration = new Trend('wishlist_duration', true);
const cartDuration = new Trend('cart_duration', true);
const fundingListDuration = new Trend('funding_list_duration', true);
const errorRate = new Rate('error_rate');

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

const SOAK_DURATION = __ENV.SOAK_DURATION || '30m';

export const options = {
    setupTimeout: '120s',
    stages: [
        { duration: '2m', target: 30 },
        { duration: SOAK_DURATION, target: 30 },
        { duration: '1m', target: 0 },
    ],
    // SLO는 Average-Load와 동일하되, 시간 경과에 따른 degradation 감지가 목적
    thresholds: {
        'http_req_duration{name:product_search}': ['p(95)<200'],
        'http_req_duration{name:product_detail}': ['p(95)<200'],
        'http_req_duration{name:wishlist}': ['p(95)<200'],
        'http_req_duration{name:cart_add}': ['p(95)<500'],
        'http_req_duration{name:funding_list}': ['p(95)<200'],
        'http_req_failed': ['rate<0.01'],
        'error_rate': ['rate<0.05'],
    },
};

export function setup() {
    if (MOCK_AUTH) {
        return { tokens: [], mockAuth: true };
    }
    const tokens = getTokens(
        accounts, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    if (tokens.length === 0) {
        throw new Error('토큰 발급 실패. Auth0 환경변수를 확인하세요.');
    }
    return { tokens, mockAuth: false };
}

export default function (data) {
    const opts = data.mockAuth
        ? mockAuthHeaders(1001 + (__VU % 100))
        : authHeaders(data.tokens[__VU % data.tokens.length]);

    // business-scenario.js와 동일한 전체 여정

    // 1. Product Search
    let productId = randomItem(PRODUCT_IDS);
    let searchOk = false;
    group('1_product_search', function () {
        const keyword = randomItem(SEARCH_KEYWORDS);
        const res = http.get(
            `${BASE_URL}/api/v2/products/search?keyword=${encodeURIComponent(keyword)}&page=0&size=10`,
            Object.assign({}, opts, { tags: { name: 'product_search' } })
        );
        searchOk = check(res, { 'search ok': (r) => r.status === 200 });
        searchDuration.add(res.timings.duration);
        errorRate.add(res.status >= 400);
        if (res.status === 200) {
            try {
                const content = res.json().data?.content;
                if (content && content.length > 0) {
                    productId = content[Math.floor(Math.random() * content.length)].id;
                }
            } catch (e) {}
        }
    });

    if (!searchOk) { sleep(1 + Math.random() * 2); return; }
    sleep(0.3 + Math.random() * 0.5);

    // 2. Product Detail
    group('2_product_detail', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/products/${productId}`,
            Object.assign({}, opts, { tags: { name: 'product_detail' } })
        );
        check(res, { 'product ok': (r) => r.status === 200 || r.status === 404 });
        productDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.3 + Math.random() * 0.5);

    // 3. Wishlist
    group('3_wishlist', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/wishlists/me?page=0&size=10`,
            Object.assign({}, opts, { tags: { name: 'wishlist' } })
        );
        check(res, { 'wishlist ok': (r) => r.status === 200 });
        wishlistDuration.add(res.timings.duration);
        errorRate.add(res.status >= 400);
    });

    sleep(0.2 + Math.random() * 0.3);

    // 4. Cart Add
    const wishItem = getRandomWishlistItem();
    group('4_cart_add', function () {
        const body = JSON.stringify({ targetId: wishItem.wishlistItemId, amount: 10000 });
        const res = http.post(
            `${BASE_URL}/api/v2/carts`, body,
            Object.assign({}, opts, { tags: { name: 'cart_add' } })
        );
        check(res, { 'cart ok': (r) => r.status === 200 || r.status === 201 || r.status === 400 });
        cartDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.2 + Math.random() * 0.3);

    // 5. Funding List
    group('5_funding_list', function () {
        const page = Math.floor(Math.random() * 3);
        const res = http.get(
            `${BASE_URL}/api/v2/fundings/list?page=${page}&size=10`,
            Object.assign({}, opts, { tags: { name: 'funding_list' } })
        );
        check(res, { 'funding list ok': (r) => r.status === 200 });
        fundingListDuration.add(res.timings.duration);
        errorRate.add(res.status >= 400);
    });

    sleep(0.5 + Math.random() * 1.0);
}

export function teardown(data) {
    console.log(`Soak Test 완료 (${SOAK_DURATION}). 인증: ${data.mockAuth ? 'Mock Auth' : 'Auth0 JWT'}`);
    console.log('Grafana/Actuator에서 heap 사용량, GC 빈도, 커넥션풀 상태를 확인하세요.');
}
