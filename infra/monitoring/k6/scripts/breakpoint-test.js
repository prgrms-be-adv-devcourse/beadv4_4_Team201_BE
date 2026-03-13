/**
 * Breakpoint Test — 시스템 한계점 탐색.
 *
 * 요청률을 지속적으로 올려서 SLO가 깨지는 지점을 찾는다.
 * abortOnFail: true로 threshold 위반 시 자동 중단.
 *
 * 결과 해석:
 * - 중단 시점의 VU 수 = 시스템 최대 동시 사용자 수
 * - 중단 시점의 req/s = 시스템 최대 처리량(throughput)
 * - 에러 유형으로 병목 지점 파악 (DB? Network? CPU?)
 *
 * k6 공식 권장: 시스템 상한선 파악 목적. 몇 차례만 실행.
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
const cartDuration = new Trend('cart_duration', true);
const errorRate = new Rate('error_rate');

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

// 20 VU에서 시작하여 10분간 300 VU까지 선형 증가
// abortOnFail: threshold 위반 시 즉시 중단
const MAX_VUS = parseInt(__ENV.MAX_VUS || '300');

export const options = {
    setupTimeout: '120s',
    stages: [
        { duration: '30s', target: 20 },
        { duration: '10m', target: MAX_VUS },
    ],
    thresholds: {
        'http_req_duration': [
            { threshold: 'p(95)<2000', abortOnFail: true, delayAbortEval: '30s' },
        ],
        'http_req_failed': [
            { threshold: 'rate<0.15', abortOnFail: true, delayAbortEval: '30s' },
        ],
    },
};

export function setup() {
    if (MOCK_AUTH) {
        console.log(`Breakpoint Test: 0 → ${MAX_VUS} VU (Mock Auth)`);
        return { tokens: [], mockAuth: true };
    }
    const tokens = getTokens(
        accounts, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    if (tokens.length === 0) {
        throw new Error('토큰 발급 실패. Auth0 환경변수를 확인하세요.');
    }
    console.log(`Breakpoint Test: 0 → ${MAX_VUS} VU (Auth0 JWT, ${tokens.length} tokens)`);
    return { tokens, mockAuth: false };
}

export default function (data) {
    const opts = data.mockAuth
        ? mockAuthHeaders(1001 + (__VU % 100))
        : authHeaders(data.tokens[__VU % data.tokens.length]);

    // 핵심 경로: 검색 → 장바구니 (최소 시나리오로 병목 탐색)

    // 1. Product Search
    group('search', function () {
        const keyword = randomItem(SEARCH_KEYWORDS);
        const res = http.get(
            `${BASE_URL}/api/v2/products/search?keyword=${encodeURIComponent(keyword)}&page=0&size=10`,
            Object.assign({}, opts, { tags: { name: 'product_search' } })
        );
        check(res, { 'search ok': (r) => r.status === 200 });
        searchDuration.add(res.timings.duration);
        errorRate.add(res.status >= 400);
    });

    sleep(0.1);

    // 2. Cart Add
    const wishItem = getRandomWishlistItem();
    group('cart', function () {
        const body = JSON.stringify({ wishlistId: wishItem.wishlistId, wishlistItemId: wishItem.wishlistItemId, amount: 10000 });
        const res = http.post(
            `${BASE_URL}/api/v2/carts`, body,
            Object.assign({}, opts, { tags: { name: 'cart_add' } })
        );
        check(res, { 'cart ok': (r) => r.status === 200 || r.status === 201 || r.status === 400 });
        cartDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.1);
}

export function teardown(data) {
    console.log('──────────────────────────────────');
    console.log('Breakpoint Test 완료');
    console.log(`목표 최대 VU: ${MAX_VUS}`);
    console.log('k6 출력에서 중단 시점의 VU 수와 req/s를 확인하세요.');
    console.log('──────────────────────────────────');
}
