/**
 * GFT-8:  위시리스트 상품 등록 (상품 검색 → 상세 → 위시리스트 등록)
 * GFT-11: 주문 목록 조회
 * GFT-12: 주문 상세 조회
 *
 * funding-scenario.js가 다루지 않는 구매자 보조 흐름.
 * Buyer accounts: 1001-1050 (Givers), 1051-1060 (Receivers)
 * VU 60: 조회 중심 시나리오. 위시리스트 등록은 20% 확률 제한.
 *
 * 실행:
 *   k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
 *          --env MOCK_AUTH=true scripts/buyer-supplement-scenario.js
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Rate, Trend } from 'k6/metrics';
import { getTokens } from '../modules/auth.js';
import {
    BASE_URL, MOCK_AUTH, PRODUCT_IDS,
    AUTH0_DOMAIN, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE,
    mockAuthHeaders, authHeaders,
    randomItem, SEARCH_KEYWORDS,
} from '../modules/config.js';

const searchDuration = new Trend('search_duration', true);
const productDetailDuration = new Trend('product_detail_duration', true);
const wishlistAddDuration = new Trend('wishlist_add_duration', true);
const wishlistCheckDuration = new Trend('wishlist_check_duration', true);
const orderListDuration = new Trend('order_list_duration', true);
const orderDetailDuration = new Trend('order_detail_duration', true);
const errorRate = new Rate('error_rate');

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

export const options = {
    setupTimeout: '120s',
    stages: [
        { duration: '15s', target: 10 },
        { duration: '60s', target: 30 },
        { duration: '120s', target: 60 },
        { duration: '30s', target: 30 },
        { duration: '15s', target: 0 },
    ],
    thresholds: {
        'http_req_duration{name:product_search}': ['p(95)<200'],
        'http_req_duration{name:product_detail}': ['p(95)<200'],
        'http_req_duration{name:wishlist_add}': ['p(95)<500'],
        'http_req_duration{name:wishlist_check}': ['p(95)<200'],
        'http_req_duration{name:order_list}': ['p(95)<200'],
        'http_req_duration{name:order_detail}': ['p(95)<200'],
        'http_req_failed': ['rate<0.05'],
        'error_rate': ['rate<0.05'],
    },
};

export function setup() {
    if (MOCK_AUTH) {
        console.log('Phase 1: Mock Auth 모드. Auth0 토큰 발급을 건너뜁니다.');
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
    const memberId = 1001 + (__VU % 50);
    const opts = data.mockAuth
        ? mockAuthHeaders(memberId)
        : authHeaders(data.tokens[__VU % data.tokens.length]);

    // ── GFT-8: 위시리스트 상품 등록 ──

    // Step 1: 상품 검색
    let productId = randomItem(PRODUCT_IDS);
    group('1_product_search', function () {
        const keyword = randomItem(SEARCH_KEYWORDS);
        const res = http.get(
            `${BASE_URL}/api/v2/products/search?keyword=${encodeURIComponent(keyword)}&page=0&size=10`,
            Object.assign({}, opts, { tags: { name: 'product_search' } })
        );
        check(res, { 'search ok': (r) => r.status === 200 });
        searchDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);

        if (res.status === 200) {
            try {
                const body = res.json();
                const content = body.data && body.data.content;
                if (content && content.length > 0) {
                    productId = randomItem(content).id;
                }
            } catch (e) { /* fallback */ }
        }
    });

    sleep(0.3 + Math.random() * 0.5);

    // Step 2: 상품 상세 조회
    group('2_product_detail', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/products/${productId}`,
            Object.assign({}, opts, { tags: { name: 'product_detail' } })
        );
        check(res, { 'product ok': (r) => r.status === 200 || r.status === 404 });
        productDetailDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.3 + Math.random() * 0.5);

    // Step 3: 위시리스트 중복 확인
    group('3_wishlist_check', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/wishlists/me/items/check?productId=${productId}`,
            Object.assign({}, opts, { tags: { name: 'wishlist_check' } })
        );
        check(res, { 'wishlist check ok': (r) => r.status === 200 });
        wishlistCheckDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.2 + Math.random() * 0.3);

    // Step 4: 위시리스트 아이템 등록 (20% 확률 — DB 쓰기 부하 제한)
    if (Math.random() < 0.2) {
        group('4_wishlist_add', function () {
            const res = http.post(
                `${BASE_URL}/api/v2/wishlists/me/items/add?productId=${productId}`,
                null,
                Object.assign({}, opts, { tags: { name: 'wishlist_add' } })
            );
            check(res, { 'wishlist add ok': (r) => r.status < 500 });
            wishlistAddDuration.add(res.timings.duration);
            errorRate.add(res.status >= 500);
        });

        sleep(0.2 + Math.random() * 0.3);
    }

    // ── GFT-11: 주문 목록 조회 ──
    let orderId = null;
    group('5_order_list', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/orders?page=0&size=10`,
            Object.assign({}, opts, { tags: { name: 'order_list' } })
        );
        check(res, { 'order list ok': (r) => r.status === 200 });
        orderListDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);

        if (res.status === 200) {
            try {
                const body = res.json();
                const content = body.data && body.data.content;
                if (content && content.length > 0) {
                    orderId = randomItem(content).orderId || randomItem(content).id;
                }
            } catch (e) { /* no order found */ }
        }
    });

    sleep(0.3 + Math.random() * 0.5);

    // ── GFT-12: 주문 상세 조회 ──
    if (orderId) {
        group('6_order_detail', function () {
            const res = http.get(
                `${BASE_URL}/api/v2/orders/${orderId}`,
                Object.assign({}, opts, { tags: { name: 'order_detail' } })
            );
            check(res, { 'order detail ok': (r) => r.status === 200 || r.status === 404 });
            orderDetailDuration.add(res.timings.duration);
            errorRate.add(res.status >= 500);
        });
    }

    sleep(0.5 + Math.random() * 1.0);
}

export function teardown(data) {
    console.log(`테스트 완료. 인증 모드: ${data.mockAuth ? 'Phase 1 (Mock Auth)' : 'Phase 2 (Auth0 JWT)'}`);
}
