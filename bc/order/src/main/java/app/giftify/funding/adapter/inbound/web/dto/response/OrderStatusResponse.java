package app.giftify.funding.adapter.inbound.web.dto.response;

public record OrderStatusResponse(
        Long orderId,
        String previousStatus,
        String currentStatus,
        String message
) {
    public static OrderStatusResponse of(Long orderId, String previousStatus, String currentStatus, String message) {
        return new OrderStatusResponse(orderId, previousStatus, currentStatus, message);
    }
}
