/**
 * Stress Test — 피크 트래픽 시뮬레이션.
 *
 * funding-scenario.js(평균 60 VU)의 200%인 120 VU까지 올려
 * 시스템이 피크 부하에서도 SLO를 유지하는지 검증.
 *
 * k6 공식 권장: Average-Load 통과 후 실행.
 * AWS Well-Architected: Stepped Load Testing 패턴 적용.
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

// Stepped Load: 30 → 60 → 90 → 120(피크) → 60 → 0
// 각 단계에서 명확한 데이터 포인트 생성 (AWS 권장)
export const options = {
    setupTimeout: '120s',
    stages: [
        { duration: '30s', target: 30 },
        { duration: '60s', target: 60 },
        { duration: '60s', target: 90 },
        { duration: '120s', target: 120 },
        { duration: '60s', target: 60 },
        { duration: '15s', target: 0 },
    ],
    thresholds: {
        'http_req_duration{name:product_search}': ['p(95)<500'],
        'http_req_duration{name:product_detail}': ['p(95)<500'],
        'http_req_duration{name:wishlist}': ['p(95)<500'],
        'http_req_duration{name:cart_add}': ['p(95)<1000'],
        'http_req_duration{name:funding_list}': ['p(95)<500'],
        'http_req_failed': ['rate<0.05'],
        'error_rate': ['rate<0.10'],
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
    sleep(0.2 + Math.random() * 0.3);

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

    sleep(0.2 + Math.random() * 0.3);

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

    sleep(0.1 + Math.random() * 0.2);

    // 4. Cart Add
    const wishItem = getRandomWishlistItem();
    group('4_cart_add', function () {
        const body = JSON.stringify({ wishlistId: wishItem.wishlistId, wishlistItemId: wishItem.wishlistItemId, amount: 10000 });
        const res = http.post(
            `${BASE_URL}/api/v2/carts`, body,
            Object.assign({}, opts, { tags: { name: 'cart_add' } })
        );
        check(res, { 'cart ok': (r) => r.status === 200 || r.status === 201 || r.status === 400 });
        cartDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.1 + Math.random() * 0.2);

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

    sleep(0.3 + Math.random() * 0.5);
}

export function teardown(data) {
    console.log(`Stress Test 완료. 인증: ${data.mockAuth ? 'Mock Auth' : 'Auth0 JWT'}`);
}
