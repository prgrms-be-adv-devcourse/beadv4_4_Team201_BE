/**
 * Preflight Smoke Test — 본 부하테스트 전 환경 검증.
 *
 * 1 VU × 1 iteration으로 각 API를 한 번씩 호출.
 * 하나라도 실패하면 threshold 위반 → exit code 99.
 *
 * 사용법 (Mock Auth):
 *   k6 run -e MOCK_AUTH=true -e BASE_URL=http://localhost:8080 \
 *     ./scripts/smoke-test.js
 *
 * 사용법 (Real Auth):
 *   k6 run -e BASE_URL=http://10.0.2.3:30080 \
 *     -e AUTH0_DOMAIN=dev-qpuevxs5iduyblx4.us.auth0.com \
 *     -e AUTH0_CLIENT_ID=<native-app-client-id> \
 *     -e AUTH0_CLIENT_SECRET=<native-app-secret> \
 *     -e AUTH0_AUDIENCE=https://api.giftify.app \
 *     ./scripts/smoke-test.js
 */
import http from 'k6/http';
import { check, group } from 'k6';
import { SharedArray } from 'k6/data';
import { getTokens } from '../modules/auth.js';
import {
    BASE_URL, MOCK_AUTH, mockAuthHeaders, authHeaders,
    AUTH0_DOMAIN, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE,
    RECEIVER_WISHLISTS,
} from '../modules/config.js';

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

export const options = {
    setupTimeout: '60s',
    vus: 1,
    iterations: 1,
    thresholds: {
        checks: ['rate==1.00'],
    },
};

export function setup() {
    if (MOCK_AUTH) {
        return { token: null, mockAuth: true };
    }

    const tokens = getTokens(
        [accounts[0]], AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    if (tokens.length === 0) {
        throw new Error('토큰 발급 실패. AUTH0_* 환경변수를 확인하세요.');
    }

    return { token: tokens[0], mockAuth: false };
}

export default function (data) {
    const opts = data.mockAuth
        ? mockAuthHeaders(1001)
        : authHeaders(data.token);

    // ── 1. Health Check ──
    group('health', function () {
        const res = http.get(`${BASE_URL}/actuator/health`);
        check(res, {
            'health 200': (r) => r.status === 200,
        });
    });

    // ── 2. Product Search (public, no auth) ──
    group('product_search', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/products/search?keyword=${encodeURIComponent('에어팟')}&page=0&size=10`,
            { headers: { 'Content-Type': 'application/json' } }
        );
        check(res, {
            'search 200': (r) => r.status === 200,
        });
    });

    // ── 3. Product Detail (public) ──
    group('product_detail', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/products/1`,
            { headers: { 'Content-Type': 'application/json' } }
        );
        check(res, {
            'product 200 or 404': (r) => r.status === 200 || r.status === 404,
        });
    });

    // ── 4. Wishlist (auth required) ──
    group('wishlist', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/wishlists/me?page=0&size=10`,
            opts
        );
        check(res, {
            'wishlist 200': (r) => r.status === 200,
        });
    });

    // ── 5. Cart Add (auth required) ──
    group('cart_add', function () {
        const item = RECEIVER_WISHLISTS[0];
        const body = JSON.stringify({ wishlistId: item.wishlistId, wishlistItemId: item.itemIds[0], amount: 10000 });
        const res = http.post(`${BASE_URL}/api/v2/carts`, body, opts);
        check(res, {
            'cart 200/201/400': (r) =>
                r.status === 200 || r.status === 201 || r.status === 400,
        });
    });

    // ── 6. Funding List (auth required) ──
    group('funding_list', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/fundings/list?page=0&size=10`,
            opts
        );
        check(res, {
            'funding 200': (r) => r.status === 200,
        });
    });
}
