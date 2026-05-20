package app.giftify.product.domain;

public enum ProductStatus {
    DRAFT, // 상품 등록 요청(승인 대기)
    REJECTED, // 관리자 등록 승인 거절 (->상품 등록을 다시 해야 함)
    ACTIVE, // 판매 중인 상태 (품절 포함)
    INACTIVE // 판매 중이 아닌 상태 (대기 상태 ex. 판매 예정인 상품입니다.)
}
