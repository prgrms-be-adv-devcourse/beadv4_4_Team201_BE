/**
 * Preflight Smoke Test — 본 부하테스트 전 환경 검증.
 *
 * 1 VU × 1 iteration으로 각 API를 한 번씩 호출.
 * 하나라도 실패하면 threshold 위반 → exit code 99.
 *
 * 사용법:
 *   k6 run -e MOCK_AUTH=true -e BASE_URL=http://localhost:8080 \
 *     ./scripts/smoke-test.js
 */
import http from 'k6/http';
import { check, group } from 'k6';
import {
    BASE_URL, authHeaders, mockAuthHeaders, MOCK_AUTH,
    RECEIVER_WISHLISTS,
} from '../modules/config.js';

export const options = {
    vus: 1,
    iterations: 1,
    thresholds: {
        checks: ['rate==1.00'],
    },
};

export default function () {
    // Mock Auth: loadtest member ID 1001 사용 (loadtest 스키마에 존재)
    // Real Auth: 환경변수로 토큰을 직접 전달
    const opts = MOCK_AUTH
        ? mockAuthHeaders(1001)
        : authHeaders(__ENV.SMOKE_TOKEN || '');

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
