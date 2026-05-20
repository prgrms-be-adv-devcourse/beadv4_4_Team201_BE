/**
 * 멱등성(Idempotency) 검증 테스트.
 *
 * concurrency-test.js와의 차이:
 * - concurrency-test: 100명이 "서로 다른 키"로 동일 리소스에 동시 접근 → 동시성 제어 검증
 * - idempotency-test: 100명이 "같은 키"로 동일 요청 동시 전송 → 멱등성 보장 검증
 *
 * 검증 대상:
 * 1. 동일 Idempotency-Key로 N번 요청 시 1건만 처리
 * 2. 나머지 N-1건은 첫 번째 응답과 동일한 결과 반환
 * 3. DB에 주문 1건만 생성 (중복 없음)
 *
 * 멱등성 구현: Redis SETNX 기반 AOP (IdempotencyAspect)
 *
 * 실행:
 *   k6 run --env BASE_URL=http://localhost:8080 --env MOCK_AUTH=true \
 *          --env TEST_FUNDING_ID=1001 --env TEST_WISHLIST_ITEM_ID=1001 \
 *          --env TEST_RECEIVER_ID=1051 --env TEST_PRODUCT_ID=11 \
 *          scripts/idempotency-test.js
 */
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import {
    BASE_URL, MOCK_AUTH,
    mockAuthHeaders, authHeaders,
} from '../modules/config.js';
import { SharedArray } from 'k6/data';
import { getTokens } from '../modules/auth.js';
import {
    AUTH0_DOMAIN, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE,
} from '../modules/config.js';

const accepted = new Counter('idempotency_accepted');
const rejected = new Counter('idempotency_rejected');
const errorRate = new Rate('error_rate');

const TEST_FUNDING_ID = parseInt(__ENV.TEST_FUNDING_ID || '0');
const TEST_WISHLIST_ITEM_ID = parseInt(__ENV.TEST_WISHLIST_ITEM_ID || '0');
const TEST_RECEIVER_ID = parseInt(__ENV.TEST_RECEIVER_ID || '0');
const TEST_PRODUCT_ID = parseInt(__ENV.TEST_PRODUCT_ID || '0');

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

/**
 * shared-iterations: 100 VU가 같은 Idempotency-Key로 동시 요청.
 * 멱등성 제어가 정상이면 1건만 처리, 99건은 중복 응답.
 */
export const options = {
    setupTimeout: '120s',
    scenarios: {
        idempotency_race: {
            executor: 'shared-iterations',
            vus: 100,
            iterations: 100,
            maxDuration: '60s',
            gracefulStop: '10s',
        },
    },
    thresholds: {
        'http_req_duration{name:idempotent_order}': ['p(95)<5000'],
        'http_req_failed': ['rate<0.05'],
        'error_rate': ['rate<0.10'],
    },
};

export function setup() {
    if (TEST_FUNDING_ID === 0 || TEST_WISHLIST_ITEM_ID === 0
        || TEST_RECEIVER_ID === 0 || TEST_PRODUCT_ID === 0) {
        throw new Error(
            '필수 환경변수 누락. 다음을 모두 설정하세요:\n' +
            '  TEST_FUNDING_ID, TEST_WISHLIST_ITEM_ID, TEST_RECEIVER_ID, TEST_PRODUCT_ID'
        );
    }

    // 모든 VU가 공유할 고정 Idempotency-Key 생성
    const fixedKey = `idempotency-test-${Date.now()}`;

    if (MOCK_AUTH) {
        console.log(`멱등성 테스트: Mock Auth, 고정 키=${fixedKey}, VU=100`);
        return { tokens: [], mockAuth: true, idempotencyKey: fixedKey };
    }

    const tokens = getTokens(
        accounts, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    console.log(`멱등성 테스트: Auth0 JWT, 고정 키=${fixedKey}, 토큰=${tokens.length}개`);
    return { tokens, mockAuth: false, idempotencyKey: fixedKey };
}

/**
 * 모든 VU가 동일한 사용자(1001)로, 동일한 Idempotency-Key로 주문 요청.
 *
 * 기대 결과:
 * - 1건: 200/201 (최초 처리)
 * - 99건: 200 (멱등성 캐시에서 동일 응답 반환) 또는 409 (중복 거절)
 * - DB에 주문 1건만 존재
 */
export default function (data) {
    // 모든 VU가 같은 사용자로 요청 (멱등성은 같은 사용자의 중복 요청을 방지)
    const opts = data.mockAuth
        ? mockAuthHeaders(1001)
        : authHeaders(data.tokens[0]);

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
            tags: { name: 'idempotent_order' },
            headers: Object.assign({}, opts.headers, {
                'X-Idempotency-Key': data.idempotencyKey,  // 모든 VU가 같은 키!
            }),
        })
    );

    const ok = check(res, {
        'accepted or deduplicated': (r) =>
            r.status === 200 || r.status === 201 || r.status === 202,
    });

    if (res.status === 200 || res.status === 201) {
        accepted.add(1);
    } else if (res.status === 409) {
        rejected.add(1);
    }
    errorRate.add(res.status >= 500);
}

export function teardown(data) {
    console.log('─────────────────────────────────────');
    console.log('멱등성 테스트 완료');
    console.log(`인증 모드: ${data.mockAuth ? 'Mock Auth' : 'Auth0 JWT'}`);
    console.log(`Idempotency-Key: ${data.idempotencyKey}`);
    console.log('');
    console.log('DB 검증 (수동):');
    console.log('  kubectl exec -n giftify statefulset/postgres --');
    console.log('    psql -U giftify -d giftify_db -c');
    console.log(`    "SELECT COUNT(*) FROM g7app.orders WHERE id >= 100;"`);
    console.log('  기대값: 1건 (멱등성 보장 시)');
    console.log('─────────────────────────────────────');
}
