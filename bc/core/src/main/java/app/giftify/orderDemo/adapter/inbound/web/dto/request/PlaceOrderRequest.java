package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

/**
 * 복수 주문 항목
 * @param items
 */
public record PlaceOrderRequest(
        @NotNull
        @Size(min = 1)
        List<PlaceOrderItemRequest> items,
        @NotNull
        PaymentMethod method
) {
        @Override
        public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                PlaceOrderRequest that = (PlaceOrderRequest) o;
                return method() == that.method() && Objects.equals(items(), that.items());
        }

        @Override
        public int hashCode() {
                return Objects.hash(items(), method());
        }
}
