package app.giftify.order.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 주문 생성 요청 DTO
@Getter
@Setter
public class CreateOrderRequest {
    @NotNull
    private Long buyerId;
    @NotEmpty
    private List<OrderItemRequest> items;
}
