package app.giftify.settlement.domain;

import java.time.LocalDateTime;

public class SettlementItemDemo {
    // 식별
    private Long id;
    private Long sellerId;
    private SettlementItemType type;
    private Long originId;

    // 스냅샷에서 복사된 근거 값
    private Long orderId;
    private Long orderItemId;
    private String paymentKey;
    private Long fundingId;

    // 회계적 증빙 / 정산 근거 관점에서 필요한 값
    private String orderNumber;
    private LocalDateTime orderedAt;
    private LocalDateTime paidAt;
    private LocalDateTime confirmedAt;

    private SettlementCore core;

    private LifeCycleMeta lifeCycleMeta;
}
