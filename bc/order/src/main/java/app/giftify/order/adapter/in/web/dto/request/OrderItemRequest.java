package app.giftify.order.adapter.in.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// 주문 아이템 요청 DTO
@Getter
@Setter
public class OrderItemRequest {
    @NotNull
    private Long fundingId;
    @NotNull
    private Long productId;
    @NotNull
    private Long sellerId;
    @NotNull
    private Long receiverId;
    @NotNull
    @Min(0)
    private Long price;
    @NotNull
    @Min(1)
    private Integer quantity;
}
