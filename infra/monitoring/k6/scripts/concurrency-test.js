/**
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
 *
 * 실행 (Mock Auth):
 *   k6 run --env BASE_URL=http://localhost:8080 --env MOCK_AUTH=true \
 *          --env TEST_FUNDING_ID=1 --env TEST_WISHLIST_ITEM_ID=1001 \
 *          --env TEST_RECEIVER_ID=1051 scripts/concurrency-test.js
 *
 * 실행 (Auth0 JWT):
 *   k6 run --env BASE_URL=... --env AUTH0_DOMAIN=... \
 *          --env TEST_FUNDING_ID=1 --env TEST_WISHLIST_ITEM_ID=1001 \
 *          --env TEST_RECEIVER_ID=1051 scripts/concurrency-test.js
 */
import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter, Rate } from 'k6/metrics';
import { getTokens } from '../modules/auth.js';
import {
    BASE_URL, MOCK_AUTH,
    AUTH0_DOMAIN, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE,
    mockAuthHeaders, authHeaders,
    generateIdempotencyKey,
} from '../modules/config.js';

const successOrders = new Counter('successful_orders');
const failedOrders = new Counter('failed_orders');
const errorRate = new Rate('error_rate');

const TEST_FUNDING_ID = parseInt(__ENV.TEST_FUNDING_ID || '0');
const TEST_WISHLIST_ITEM_ID = parseInt(__ENV.TEST_WISHLIST_ITEM_ID || '0');
const TEST_RECEIVER_ID = parseInt(__ENV.TEST_RECEIVER_ID || '0');
const TEST_PRODUCT_ID = parseInt(__ENV.TEST_PRODUCT_ID || '0');

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

/**
 * shared-iterations: 총 iterations를 VU들이 풀에서 꺼내 실행.
 * 100 VU가 각 1번씩 = 100건이 최대한 동시에 도착.
 * ramping-vus 대신 shared-iterations를 쓰는 이유:
 * - ramping-vus는 시간에 따라 VU가 서서히 증가 → 부하 분산됨
 * - shared-iterations는 모든 VU가 "동시에" 시작 → 경합 극대화
 */
export const options = {
    setupTimeout: '120s',
    scenarios: {
        funding_race: {
            executor: 'shared-iterations',
            vus: 100,
            iterations: 100,
            maxDuration: '60s',
            gracefulStop: '10s',
        },
    },
    thresholds: {
        'http_req_duration{name:concurrent_order}': ['p(95)<1000'],
        'http_req_failed': ['rate<0.05'],
        'error_rate': ['rate<0.10'],
    },
};

export function setup() {
    if (TEST_FUNDING_ID === 0 || TEST_WISHLIST_ITEM_ID === 0 || TEST_RECEIVER_ID === 0 || TEST_PRODUCT_ID === 0) {
        throw new Error(
            '필수 환경변수 누락. 다음을 모두 설정하세요:\n' +
            '  TEST_FUNDING_ID, TEST_WISHLIST_ITEM_ID, TEST_RECEIVER_ID, TEST_PRODUCT_ID'
        );
    }

    if (MOCK_AUTH) {
        console.log(`Phase 1: Mock Auth 모드. 펀딩 ID: ${TEST_FUNDING_ID}, VU: 100`);
        return { tokens: [], mockAuth: true };
    }

    const tokens = getTokens(
        accounts, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    if (tokens.length === 0) {
        throw new Error('토큰 발급 실패. Auth0 환경변수를 확인하세요.');
    }

    console.log(`Phase 2: Auth0 JWT 모드. 펀딩 ID: ${TEST_FUNDING_ID}, 토큰: ${tokens.length}개`);
    return { tokens, mockAuth: false };
}

/**
 * 각 VU는 고유 사용자로 동작하여 "서로 다른 100명"이
 * "동일한 펀딩"에 동시 참여하는 상황을 재현한다.
 *
 * 주문 금액 1,000원 고정:
 * target_amount 100,000원 펀딩에 100명이 1,000원씩 → 딱 100% 달성
 * 동시성 제어가 정상이면 current_amount == 100,000원.
 * 깨졌다면 초과 → verify.sh에서 감지.
 */
export default function (data) {
    const opts = data.mockAuth
        ? mockAuthHeaders(1001 + (__VU % 100))
        : authHeaders(data.tokens[__VU % data.tokens.length]);

    const body = JSON.stringify({
        items: [{
            wishlistItemId: TEST_WISHLIST_ITEM_ID,
            productId: TEST_PRODUCT_ID,
            fundingId: TEST_FUNDING_ID,
            receiverId: TEST_RECEIVER_ID,
            amount: 1000,
            orderItemType: 'FUNDING_GIFT',
        }],
        method: 'DEPOSIT',
    });

    const res = http.post(
        `${BASE_URL}/api/v2/orders`,
        body,
        Object.assign({}, opts, {
            tags: { name: 'concurrent_order' },
            headers: Object.assign({}, opts.headers, {'X-Idempotency-Key': generateIdempotencyKey()})
        })
    );

    check(res, {
        'order succeeded or rejected': (r) =>
            r.status === 200 || r.status === 201 || r.status === 400,
    });

    if (res.status === 200 || res.status === 201) {
        successOrders.add(1);
    } else {
        failedOrders.add(1);
    }
    errorRate.add(res.status >= 500);
}

export function teardown(data) {
    console.log('─────────────────────────────────────');
    console.log('동시성 테스트 완료');
    console.log(`인증 모드: ${data.mockAuth ? 'Phase 1 (Mock Auth)' : 'Phase 2 (Auth0 JWT)'}`);
    console.log(`대상 펀딩 ID: ${TEST_FUNDING_ID}`);
    if (!data.mockAuth) {
        console.log(`사용된 토큰: ${data.tokens.length}개`);
    }
    console.log('');
    console.log('다음 명령으로 DB 정합성을 검증하세요:');
    console.log(`  ./infra/monitoring/k6/verify.sh ${TEST_FUNDING_ID}`);
    console.log('─────────────────────────────────────');
}
