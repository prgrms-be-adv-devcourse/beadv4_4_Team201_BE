package app.giftify.settlement.domain;

import java.time.LocalDateTime;

public class PaymentSnapshot {
    private Long orderId;
    private String paymentKey;
    private String transactionKey;
    private LocalDateTime paidAt;
}
