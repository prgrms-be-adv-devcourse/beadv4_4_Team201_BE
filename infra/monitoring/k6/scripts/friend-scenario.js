/**
 * GFT-1: 펀딩 선물 결정 (친구 목록 → 친구 위시리스트 → 상품 상세)
 * GFT-6: 친구 추가 (친구 요청 → 수락)
 *
 * Seed data: Givers(1001-1050) ↔ Receivers(1051-1060) = 500 ACCEPTED friendships
 * VU 60: 조회 중심 시나리오이므로 인프라 한계까지 활용. 쓰기(GFT-6)는 10% 확률 제한.
 *
 * 실행:
 *   k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
 *          --env MOCK_AUTH=true scripts/friend-scenario.js
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
    randomItem, GIVER_IDS, RECEIVER_IDS,
} from '../modules/config.js';

const friendListDuration = new Trend('friend_list_duration', true);
const friendWishlistDuration = new Trend('friend_wishlist_duration', true);
const productDetailDuration = new Trend('product_detail_duration', true);
const friendRequestDuration = new Trend('friend_request_duration', true);
const friendAcceptDuration = new Trend('friend_accept_duration', true);
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
        'http_req_duration{name:friend_list}': ['p(95)<200'],
        'http_req_duration{name:friend_wishlist}': ['p(95)<200'],
        'http_req_duration{name:product_detail}': ['p(95)<200'],
        'http_req_duration{name:friend_request}': ['p(95)<500'],
        'http_req_duration{name:friend_accept}': ['p(95)<500'],
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
    const giverId = GIVER_IDS[__VU % GIVER_IDS.length];
    const buyerOpts = data.mockAuth
        ? mockAuthHeaders(giverId)
        : authHeaders(data.tokens[__VU % data.tokens.length]);

    // ── GFT-1: 펀딩 선물 결정 ──

    // Step 1: 친구 목록 조회
    let friendMemberId = randomItem(RECEIVER_IDS);
    group('1_friend_list', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/friends`,
            Object.assign({}, buyerOpts, { tags: { name: 'friend_list' } })
        );
        check(res, { 'friend list ok': (r) => r.status === 200 });
        friendListDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);

        if (res.status === 200) {
            try {
                const body = res.json();
                const friends = body.data;
                if (friends && friends.length > 0) {
                    const friend = randomItem(friends);
                    friendMemberId = friend.id || friendMemberId;
                }
            } catch (e) { /* fallback to seed receiver */ }
        }
    });

    sleep(0.3 + Math.random() * 0.5);

    // Step 2: 친구의 위시리스트 조회
    let productId = null;
    group('2_friend_wishlist', function () {
        const res = http.get(
            `${BASE_URL}/api/v2/wishlists/${friendMemberId}?page=0&size=10`,
            Object.assign({}, buyerOpts, { tags: { name: 'friend_wishlist' } })
        );
        check(res, { 'friend wishlist ok': (r) => r.status === 200 || r.status === 403 });
        friendWishlistDuration.add(res.timings.duration);
        errorRate.add(res.status >= 500);

        if (res.status === 200) {
            try {
                const body = res.json();
                const content = body.data && body.data.content;
                if (content && content.length > 0) {
                    const item = randomItem(content);
                    productId = item.productId || item.id;
                }
            } catch (e) { /* no product extraction */ }
        }
    });

    sleep(0.3 + Math.random() * 0.5);

    // Step 3: 상품 상세 조회
    if (productId) {
        group('3_product_detail', function () {
            const res = http.get(
                `${BASE_URL}/api/v2/products/${productId}`,
                Object.assign({}, buyerOpts, { tags: { name: 'product_detail' } })
            );
            check(res, { 'product ok': (r) => r.status === 200 || r.status === 404 });
            productDetailDuration.add(res.timings.duration);
            errorRate.add(res.status >= 500);
        });

        sleep(0.3 + Math.random() * 0.5);
    }

    // ── GFT-6: 친구 추가 (10% 확률로 실행 — DB 쓰기 부하 제한) ──
    if (Math.random() < 0.1) {
        const targetId = 1051 + Math.floor(Math.random() * 10);

        // Step 4: 친구 요청
        let friendshipId = null;
        group('4_friend_request', function () {
            const body = JSON.stringify({ receiverId: targetId });
            const res = http.post(
                `${BASE_URL}/api/v2/friends/request`,
                body,
                Object.assign({}, buyerOpts, { tags: { name: 'friend_request' } })
            );
            check(res, { 'friend request ok': (r) => r.status < 500 });
            friendRequestDuration.add(res.timings.duration);
            errorRate.add(res.status >= 500);

            if (res.status === 200 || res.status === 201) {
                try {
                    const respBody = res.json();
                    friendshipId = respBody.data && (respBody.data.friendshipId || respBody.data.id);
                } catch (e) { /* skip accept */ }
            }
        });

        sleep(0.2 + Math.random() * 0.3);

        // Step 5: 친구 수락 (receiver 관점)
        if (friendshipId) {
            const receiverOpts = data.mockAuth
                ? mockAuthHeaders(targetId)
                : authHeaders(data.tokens[targetId % data.tokens.length]);

            group('5_friend_accept', function () {
                const res = http.post(
                    `${BASE_URL}/api/v2/friends/${friendshipId}/accept`,
                    null,
                    Object.assign({}, receiverOpts, { tags: { name: 'friend_accept' } })
                );
                check(res, { 'friend accept ok': (r) => r.status < 500 });
                friendAcceptDuration.add(res.timings.duration);
                errorRate.add(res.status >= 500);
            });
        }
    }

    sleep(0.5 + Math.random() * 1.0);
}

export function teardown(data) {
    console.log(`테스트 완료. 인증 모드: ${data.mockAuth ? 'Phase 1 (Mock Auth)' : 'Phase 2 (Auth0 JWT)'}`);
}
