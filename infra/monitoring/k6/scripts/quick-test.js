/**
 * Quick Test — 30초 단축 시나리오.
 * funding-scenario.js와 동일한 여정을 5 VU × 30초로 실행.
 * 빠른 반복 디버깅용.
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Rate } from 'k6/metrics';
import { getTokens } from '../modules/auth.js';
import {
    AUTH0_AUDIENCE, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET,
    AUTH0_DOMAIN, authHeaders, mockAuthHeaders, MOCK_AUTH,
    BASE_URL, PRODUCT_IDS, randomItem, SEARCH_KEYWORDS,
    getRandomWishlistItem,
} from '../modules/config.js';

const errorRate = new Rate('error_rate');

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

export const options = {
    setupTimeout: '120s',
    vus: 5,
    duration: '30s',
    thresholds: {
        'checks': ['rate>0.90'],
        'http_req_failed': ['rate<0.10'],
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
    group('search', function () {
        const keyword = randomItem(SEARCH_KEYWORDS);
        const res = http.get(
            `${BASE_URL}/api/v2/products/search?keyword=${encodeURIComponent(keyword)}&page=0&size=10`,
            Object.assign({}, opts, { tags: { name: 'product_search' } })
        );
        const ok = check(res, { 'search 200': (r) => r.status === 200 });
        errorRate.add(res.status >= 400);
        if (ok && res.status === 200) {
            try {
                const content = res.json().data?.content;
                if (content && content.length > 0) {
                    productId = content[Math.floor(Math.random() * content.length)].id;
                }
            } catch (e) {}
        }
    });

    sleep(0.3);

    // 2. Product Detail
    group('product', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/products/${productId}`,
            Object.assign({}, opts, { tags: { name: 'product_detail' } })
        );
        check(res, { 'product 200|404': (r) => r.status === 200 || r.status === 404 });
        errorRate.add(res.status >= 500);
    });

    sleep(0.3);

    // 3. Wishlist
    group('wishlist', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/wishlists/me?page=0&size=10`,
            opts
        );
        check(res, { 'wishlist 200': (r) => r.status === 200 });
        errorRate.add(res.status >= 400);
    });

    sleep(0.2);

    // 4. Cart Add
    group('cart', function () {
        const wishItem = getRandomWishlistItem();
        const body = JSON.stringify({ wishlistId: wishItem.wishlistId, wishlistItemId: wishItem.wishlistItemId, amount: 10000 });
        const res = http.post(
            `${BASE_URL}/api/v2/carts`, body,
            Object.assign({}, opts, { tags: { name: 'cart_add' } })
        );
        const ok = check(res, {
            'cart 200|201|400': (r) =>
                r.status === 200 || r.status === 201 || r.status === 400,
        });
        if (!ok) {
            console.log(`cart fail: VU=${__VU} memberId=${1001 + (__VU % 100)} wishlistItemId=${wishItem.wishlistItemId} status=${res.status} body=${res.body}`);
        }
        errorRate.add(res.status >= 500);
    });

    sleep(0.2);

    // 5. Funding List
    group('funding', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/fundings/list?page=0&size=10`,
            Object.assign({}, opts, { tags: { name: 'funding_list' } })
        );
        check(res, { 'funding 200': (r) => r.status === 200 });
        errorRate.add(res.status >= 400);
    });

    sleep(0.3);
}
