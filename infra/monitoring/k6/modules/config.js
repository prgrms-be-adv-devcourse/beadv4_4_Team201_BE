export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const AUTH0_DOMAIN = __ENV.AUTH0_DOMAIN;
export const AUTH0_CLIENT_ID = __ENV.AUTH0_CLIENT_ID;
export const AUTH0_CLIENT_SECRET = __ENV.AUTH0_CLIENT_SECRET;
export const AUTH0_AUDIENCE = __ENV.AUTH0_AUDIENCE;
export const CF_ACCESS_CLIENT_ID = __ENV.CF_ACCESS_CLIENT_ID;
export const CF_ACCESS_CLIENT_SECRET = __ENV.CF_ACCESS_CLIENT_SECRET;

// Phase 1 (Mock Auth) 모드 스위치.
// MOCK_AUTH=true면 Auth0 토큰 대신 X-Test-User-ID 헤더를 사용한다.
// DynamicMockAuthFilter가 이 헤더를 읽어 SecurityContext에 주입.
export const MOCK_AUTH = (__ENV.MOCK_AUTH || 'false').toLowerCase() === 'true';

// __ENV는 -e CLI 플래그로 주입된 환경변수 전용 객체. Node.js의 process.env와 유사하지만 k6 런타임 전용

export const SEARCH_KEYWORDS = ['에어팟', '스타벅스', '닌텐도', '다이슨', '레고', '랜턴', '고양이', '스피커'];
export const PRODUCT_IDS = [11, 14, 25, 30, 38, 49, 58, 66, 76, 85, 91, 98];

// Phase 2: Auth0 실제 JWT 토큰 + CF-Access 헤더
export function authHeaders(token) {
    const headers = {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
    };

    if (CF_ACCESS_CLIENT_ID && CF_ACCESS_CLIENT_SECRET) {
        headers['CF-Access-Client-Id'] = CF_ACCESS_CLIENT_ID;
        headers['CF-Access-Client-Secret'] = CF_ACCESS_CLIENT_SECRET;
    }

    return { headers };
}

// Phase 1: DynamicMockAuthFilter용 헤더. JWT 없이 사용자를 식별한다.
// memberId는 VU 번호 기반으로 생성하여 VU별로 다른 사용자로 동작.
export function mockAuthHeaders(memberId, role = 'BUYER') {
    const headers = {
        'X-Test-User-ID': String(memberId),
        'X-Test-User-ROLE': role,
        'Content-Type': 'application/json',
    };

    if (CF_ACCESS_CLIENT_ID && CF_ACCESS_CLIENT_SECRET) {
        headers['CF-Access-Client-Id'] = CF_ACCESS_CLIENT_ID;
        headers['CF-Access-Client-Secret'] = CF_ACCESS_CLIENT_SECRET;
    }

    return { headers };
}

export function randomItem(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

// Loadtest member ranges
// Givers: 1001-1050 (BUYER), Receivers: 1051-1060 (BUYER), Sellers: 1101-1110 (SELLER)
export const GIVER_IDS = Array.from({length: 50}, (_, i) => 1001 + i);
export const RECEIVER_IDS = Array.from({length: 10}, (_, i) => 1051 + i);
export const SELLER_IDS = Array.from({length: 10}, (_, i) => 1101 + i);

export const PRODUCT_CATEGORIES = ['ELECTRONICS', 'BEAUTY', 'FASHION', 'LIVING', 'FOODS', 'TOYS', 'OUTDOOR', 'PET', 'KITCHEN'];

// Receiver wishlist mapping: Receivers(1051-1060)의 위시리스트 아이템
// wishlist_id == member_id (seed 규칙), itemIds = 1001~1100 (10개씩)
export const RECEIVER_WISHLISTS = [
    { memberId: 1051, wishlistId: 1051, itemIds: [1001,1002,1003,1004,1005,1006,1007,1008,1009,1010] },
    { memberId: 1052, wishlistId: 1052, itemIds: [1011,1012,1013,1014,1015,1016,1017,1018,1019,1020] },
    { memberId: 1053, wishlistId: 1053, itemIds: [1021,1022,1023,1024,1025,1026,1027,1028,1029,1030] },
    { memberId: 1054, wishlistId: 1054, itemIds: [1031,1032,1033,1034,1035,1036,1037,1038,1039,1040] },
    { memberId: 1055, wishlistId: 1055, itemIds: [1041,1042,1043,1044,1045,1046,1047,1048,1049,1050] },
    { memberId: 1056, wishlistId: 1056, itemIds: [1051,1052,1053,1054,1055,1056,1057,1058,1059,1060] },
    { memberId: 1057, wishlistId: 1057, itemIds: [1061,1062,1063,1064,1065,1066,1067,1068,1069,1070] },
    { memberId: 1058, wishlistId: 1058, itemIds: [1071,1072,1073,1074,1075,1076,1077,1078,1079,1080] },
    { memberId: 1059, wishlistId: 1059, itemIds: [1081,1082,1083,1084,1085,1086,1087,1088,1089,1090] },
    { memberId: 1060, wishlistId: 1060, itemIds: [1091,1092,1093,1094,1095,1096,1097,1098,1099,1100] },
];

// 랜덤 Receiver의 랜덤 위시리스트 아이템을 반환
export function getRandomWishlistItem() {
    const receiver = randomItem(RECEIVER_WISHLISTS);
    return {
        receiverId: receiver.memberId,
        wishlistId: receiver.wishlistId,
        wishlistItemId: randomItem(receiver.itemIds),
    };
}
