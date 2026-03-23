/**
 * GFT-5: 상품 판매 (판매자 상품 등록 → 내 상품 목록 조회)
 * GFT-13: 정산 목록 조회
 *
 * SELLER 역할 전용 시나리오. Mock Auth에서 role='SELLER'로 동작.
 * Seller accounts: 1101-1110 (seed data)
 * VU 30: 쓰기(상품 등록) 50% 확률 제한 적용. SELLER 10명이 라운드로빈.
 *
 * 실행:
 *   k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
 *          --env MOCK_AUTH=true scripts/seller-scenario.js
 */
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Rate, Trend } from 'k6/metrics';
import { getTokens } from '../modules/auth.js';
import {
    BASE_URL, MOCK_AUTH,
    AUTH0_DOMAIN, AUTH0_CLIENT_ID, AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE,
    mockAuthHeaders, authHeaders,
    randomItem, SELLER_IDS, PRODUCT_CATEGORIES,
} from '../modules/config.js';

const productCreateDuration = new Trend('product_create_duration', true);
const myProductsDuration = new Trend('my_products_duration', true);
const settlementListDuration = new Trend('settlement_list_duration', true);
const errorRate = new Rate('error_rate');

const accounts = new SharedArray('accounts', function () {
    return JSON.parse(open('../data/test-accounts.json'));
});

const PRODUCT_NAMES = [
    '에어팟 프로 2세대', '스타벅스 텀블러', '닌텐도 스위치 케이스',
    '다이슨 에어랩', '레고 테크닉', '캠핑 랜턴', '고양이 장난감 세트',
    'JBL 블루투스 스피커', '무선 충전 패드', '아이패드 거치대',
];

export const options = {
    setupTimeout: '120s',
    stages: [
        { duration: '15s', target: 10 },
        { duration: '60s', target: 20 },
        { duration: '120s', target: 30 },
        { duration: '30s', target: 10 },
        { duration: '15s', target: 0 },
    ],
    thresholds: {
        'http_req_duration{name:product_create}': ['p(95)<500'],
        'http_req_duration{name:my_products}': ['p(95)<200'],
        'http_req_duration{name:settlement_list}': ['p(95)<200'],
        'http_req_failed': ['rate<0.05'],
        'error_rate': ['rate<0.05'],
    },
};

export function setup() {
    if (MOCK_AUTH) {
        console.log('Phase 1: Mock Auth 모드 (SELLER). Auth0 토큰 발급을 건너뜁니다.');
        return { tokens: [], mockAuth: true };
    }

    const sellerAccounts = accounts.filter(a => a.role === 'SELLER');
    if (sellerAccounts.length === 0) {
        throw new Error('SELLER 계정이 test-accounts.json에 없습니다.');
    }

    const tokens = getTokens(
        sellerAccounts, AUTH0_DOMAIN, AUTH0_CLIENT_ID,
        AUTH0_CLIENT_SECRET, AUTH0_AUDIENCE
    );
    if (tokens.length === 0) {
        throw new Error('토큰 발급 실패. Auth0 환경변수를 확인하세요.');
    }

    console.log(`Phase 2: Auth0 JWT 모드 (SELLER). 토큰: ${tokens.length}개`);
    return { tokens, mockAuth: false };
}

export default function (data) {
    const sellerId = SELLER_IDS[__VU % SELLER_IDS.length];
    const sellerOpts = data.mockAuth
        ? mockAuthHeaders(sellerId, 'SELLER')
        : authHeaders(data.tokens[__VU % data.tokens.length]);

    // ── GFT-5: 상품 판매 ──

    // Step 1: 상품 등록 (50% 확률 — DB 쓰기 부하 제한)
    if (Math.random() < 0.5) {
        group('1_product_create', function () {
            const body = JSON.stringify({
                name: `${randomItem(PRODUCT_NAMES)} #${Date.now() % 10000}`,
                description: `부하테스트 상품 - VU${__VU} Iter${__ITER}`,
                price: (Math.floor(Math.random() * 20) + 1) * 5000,
                stock: Math.floor(Math.random() * 100) + 10,
                category: randomItem(PRODUCT_CATEGORIES),
                imageKey: null,
            });
            const res = http.post(
                `${BASE_URL}/api/v2/products`,
                body,
                Object.assign({}, sellerOpts, { tags: { name: 'product_create' } })
            );
            check(res, { 'product created': (r) => r.status === 200 || r.status === 201 });
            productCreateDuration.add(res.timings.duration);
            errorRate.add(res.status >= 500);
        });

        sleep(0.3 + Math.random() * 0.5);
    }

    // Step 2: 내 상품 목록 조회
    group('2_my_products', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/products/my?page=0&size=10`,
            Object.assign({}, sellerOpts, { tags: { name: 'my_products' } })
        );
        check(res, { 'my products ok': (r) => r.status === 200 });
        myProductsDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.3 + Math.random() * 0.5);

    // ── GFT-13: 정산 목록 조회 ──
    group('3_settlement_list', function () {
        const res = http.get(
            `${BASE_URL}/api/v1/settlements?page=0&size=10`,
            Object.assign({}, sellerOpts, { tags: { name: 'settlement_list' } })
        );
        check(res, { 'settlement list ok': (r) => r.status === 200 || r.status === 403 });
        settlementListDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);
    });

    sleep(0.5 + Math.random() * 1.0);
}

export function teardown(data) {
    console.log(`테스트 완료. 인증 모드: ${data.mockAuth ? 'Phase 1 (Mock Auth)' : 'Phase 2 (Auth0 JWT)'}`);
}
