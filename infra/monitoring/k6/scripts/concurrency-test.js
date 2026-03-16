/**
 * ★ Insight ─────────────────────────────────────
 * 동시성 경합(Race Condition) 전용 테스트.
 *
 * funding-scenario.js와의 차이:
 * - funding-scenario: 다양한 API를 순차 호출하며 "부하" 측정
 * - concurrency-test: 동일 리소스에 100명이 동시 접근하여 "정합성" 검증
 *
 * 핵심 executor: "shared-iterations"
 * - 총 반복 횟수를 모든 VU가 나눠 가짐
 * - 100 VU × 1 iteration = 100건이 거의 동시에 실행
 * - ramping-vus와 달리 "동시 폭주"를 의도적으로 만드는 executor
 *
 * 검증 대상:
 * 1. 펀딩 초과 방지: current_amount <= target_amount
 * 2. 재고 음수 방지: stock >= 0
 * 3. 중복 참여 방지: 동일 사용자가 같은 펀딩에 2번 이상 참여 불가
 * → DB 검증은 k6가 아닌 verify.sh로 수행 (k6에 PostgreSQL 클라이언트 없음)
 * ─────────────────────────────────────────────────
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter, Rate } from 'k6/metrics';
import { getTokens } from '../modules/auth.js';
import {
    BASE_URL, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
    AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE,
    authHeaders,
} from '../modules/config.js';

// -- 커스텀 메트릭 --
// Counter: 성공/실패 건수를 누적 (Rate와 달리 절대값)
const successOrders = new Counter('successful_orders');
const failedOrders = new Counter('failed_orders');
const errorRate = new Rate('error_rate');

// -- 환경변수: 테스트 대상 리소스 --
// prod DB에서 사전 조회하여 주입해야 함
const TEST_FUNDING_ID = parseInt(__ENV.TEST_FUNDING_ID || '0');
const TEST_WISHLIST_ITEM_ID = parseInt(__ENV.TEST_WISHLIST_ITEM_ID || '0');
const TEST_RECEIVER_ID = parseInt(__ENV.TEST_RECEIVER_ID || '0');

// -- 테스트 계정 로드 --
const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

/**
 * ★ Executor 설계 ─────────────────────────────────
 * shared-iterations: 총 iterations를 VU들이 풀에서 꺼내 실행.
 * 100 VU가 각 1번씩 = 100건이 최대한 동시에 도착.
 *
 * maxDuration: 안전장치. 모든 iteration이 이 시간 내에 끝나야 함.
 * gracefulStop: iteration 중간에 강제 종료하지 않는 유예 시간.
 *
 * ramping-vus 대신 shared-iterations를 쓰는 이유:
 * - ramping-vus는 시간에 따라 VU가 서서히 증가 → 부하 분산됨
 * - shared-iterations는 모든 VU가 "동시에" 시작 → 경합 극대화
 * ─────────────────────────────────────────────────
 */
export const options = {
    setupTimeout: '120s',
    scenarios: {
        funding_race: {
            executor: 'shared-iterations',
            vus: 100,
            iterations: 100,        // 100 VU × 1회 = 100건 동시 주문
            maxDuration: '60s',
            gracefulStop: '10s',
        },
    },
    thresholds: {
        'http_req_duration{name:concurrent_order}': ['p(95)<1000'],
        'http_req_failed': ['rate<0.05'], // 경합 상황이므로 5%까지 허용
        'error_rate': ['rate<0.10'],
    },
};

// -- Setup: 토큰 벌크 발급 --
export function setup() {
    // 필수 환경변수 검증
    if (TEST_FUNDING_ID === 0 || TEST_WISHLIST_ITEM_ID === 0 || TEST_RECEIVER_ID === 0) {
        throw new Error(
            '필수 환경변수 누락. 다음을 모두 설정하세요:\n' +
            '  TEST_FUNDING_ID, TEST_WISHLIST_ITEM_ID, TEST_RECEIVER_ID'
        );
    }

    const tokens = getTokens(
        accounts, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    if (tokens.length === 0) {
        throw new Error('토큰 발급 실패. Auth0 환경변수를 확인하세요.');
    }

    console.log(`동시성 테스트 시작 — 펀딩 ID: ${TEST_FUNDING_ID}, VU: 100`);
    return { tokens };
}

/**
 * ★ 설계 의도 ─────────────────────────────────────
 * 각 VU는 고유 토큰을 사용하므로 "서로 다른 100명"이
 * "동일한 펀딩"에 동시 참여하는 상황을 재현한다.
 *
 * 주문 금액을 1,000원으로 고정:
 * target_amount가 100,000원인 펀딩에 100명이 1,000원씩 → 딱 100% 달성
 * 동시성 제어가 제대로 되어있다면 current_amount는 정확히 100,000원.
 * 깨졌다면 100,000원 초과 → verify.sh에서 감지.
 * ─────────────────────────────────────────────────
 */
export default function (data) {
    const token = data.tokens[__VU % data.tokens.length];
    const opts = authHeaders(token);

    // 동일 펀딩에 대한 주문 생성 — 경합 발생 지점
    const body = JSON.stringify({
        items: [{
            wishlistItemId: TEST_WISHLIST_ITEM_ID,
            receiverId: TEST_RECEIVER_ID,
            amount: 1000,
            orderItemType: 'FUNDING_GIFT',
        }],
        method: 'DEPOSIT',
    });

    const res = http.post(
        `${BASE_URL}/api/v2/orders`,
        body,
        Object.assign({}, opts, { tags: { name: 'concurrent_order' } })
    );

    const ok = check(res, {
        'order succeeded or rejected': (r) =>
            r.status === 200 || r.status === 201 || r.status === 400,
    });

    if (res.status === 200 || res.status === 201) {
        successOrders.add(1);
    } else {
        failedOrders.add(1);
    }
    // 5xx만 진짜 에러. 400은 비즈니스 거부(펀딩 마감, 중복 참여 등)이므로 정상
    errorRate.add(res.status >= 500);
}

// -- Teardown --
export function teardown(data) {
    console.log('─────────────────────────────────────');
    console.log('동시성 테스트 완료');
    console.log(`대상 펀딩 ID: ${TEST_FUNDING_ID}`);
    console.log(`사용된 토큰: ${data.tokens.length}개`);
    console.log('');
    console.log('다음 명령으로 DB 정합성을 검증하세요:');
    console.log(`  ./infra/monitoring/k6/verify.sh ${TEST_FUNDING_ID}`);
    console.log('─────────────────────────────────────');
}
