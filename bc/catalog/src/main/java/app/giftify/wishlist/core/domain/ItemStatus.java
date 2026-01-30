//package app.giftify.wishlist.core.domain;
//
//import app.giftify.wishlist.core.domain.exception.InvalidWishlistItemStatusException;
//
//public enum ItemStatus {
//    DRAFT("상품 등록 대기 상태입니다."),
//    INACTIVE("관리자 상품 등록 승인 또는 상품 판매 중지 상태입니다."),
//    REJECTED("관리자 상품 등록이 거절된 상태입니다."),
//    ACTIVE("판매 진행 중인 상품입니다.");
//
//    private final String description;
//
//    ItemStatus(String description) {
//        this.description = description;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public static ItemStatus from(String value) {
//        try {
//            return ItemStatus.valueOf(value.toUpperCase());
//        } catch (IllegalArgumentException | NullPointerException e) {
//            throw new InvalidWishlistItemStatusException(value);
//        }
//    }
//}
