package app.giftify.funding.domain.vo;

/**
 * 개별 펀딩 상세 정보
 * @param fundingId 생성된 펀딩 ID
 * @param orderItemId 펀딩과 연결된 주문 상품 ID
 * @param wishlistItemId 펀딩과 연결된 위시리스트 아이템 ID
 */
public record FundingDetail(
        Long fundingId,
        Long orderItemId,
        Long wishlistItemId,
        Long receiverId
) {
}
