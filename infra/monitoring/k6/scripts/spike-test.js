/**
 * Spike Test — 갑작스러운 트래픽 폭증 시뮬레이션.
 *
 * 이벤트 오픈, 타임세일, SNS 바이럴 등으로 인한
 * 순간 트래픽 급증 시 시스템 생존 여부 검증.
 *
 * k6 권장: ramping-arrival-rate executor 사용.
 * VU 기반이 아닌 요청률(requests/sec) 기반으로
 * 서버 응답 속도와 무관하게 일정한 부하를 가한다.
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

// ramping-arrival-rate: 초당 요청 수 기반 부하 제어
// 정상(10 req/s) → 급증(100 req/s) → 유지 → 복귀
export const options = {
    setupTimeout: '120s',
    scenarios: {
        spike: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            preAllocatedVUs: 50,
            maxVUs: 200,
            stages: [
                { target: 10, duration: '30s' },
                { target: 100, duration: '10s' },
                { target: 100, duration: '60s' },
                { target: 10, duration: '10s' },
                { target: 10, duration: '30s' },
            ],
        },
    },
    thresholds: {
        'http_req_duration{name:product_search}': ['p(95)<1000'],
        'http_req_duration{name:cart_add}': ['p(95)<2000'],
        'http_req_failed': ['rate<0.10'],
        'error_rate': ['rate<0.15'],
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

    // Spike 시나리오: 핵심 경로만 빠르게 반복
    // 검색 → 장바구니 (가장 부하가 큰 쓰기 경로)

    // 1. Product Search
    let productId = randomItem(PRODUCT_IDS);
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
}

export function teardown(data) {
    console.log(`Spike Test 완료. 인증: ${data.mockAuth ? 'Mock Auth' : 'Auth0 JWT'}`);
}
