package app.giftify.settlement.application;

import app.giftify.settlement.application.inbound.InitializeSettlementItemCommand;
import app.giftify.settlement.application.outbound.port.OrderItemSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderSnapshotRepository;
import app.giftify.settlement.application.outbound.port.PaymentSnapshotRepository;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.*;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.DomainException;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementItemServiceTest {

    @Mock
    private SettlementItemRepository settlementItemRepository;

    @Mock
    private OrderSnapshotRepository orderSnapshotRepository;

    @Mock
    private OrderItemSnapshotRepository orderItemSnapshotRepository;

    @Mock
    private PaymentSnapshotRepository paymentSnapshotRepository;

    @Mock
    private FeePolicyService feePolicyService;

    @InjectMocks
    private SettlementItemService settlementItemService;

    @Test
    @DisplayName("정산 아이템 초기화: 스냅샷과 수수료 정책으로 정산 아이템을 생성해 저장한다")
    void initializeSettlementItem_createsSettlementItemFromSnapshots() {
        Long targetId = 10L;
        TargetType targetType = TargetType.FUNDING;
        OrderItemType orderItemType = OrderItemType.FUNDING_GIFT;
        Long orderItemId = 20L;
        Long orderId = 30L;
        Long sellerId = 40L;
        Long paymentId = 50L;
        String orderNumber = "ORD-20240101-001";

        LocalDateTime orderedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime paidAt = orderedAt.plusHours(1);
        LocalDateTime confirmedAt = paidAt.plusHours(2);

        OrderItemSnapshot itemSnapshot = new OrderItemSnapshot(
                orderItemId,
                orderId,
                targetId,
                targetType,
                orderItemType,
                sellerId,
                Money.of("5000"),
                Money.of("10000")
        );
        OrderSnapshot orderSnapshot = new OrderSnapshot(orderId, orderNumber, orderedAt);
        PaymentSnapshot paymentSnapshot = new PaymentSnapshot(
            paymentId,
            orderNumber,
            "payment-key",
            "transaction-key",
            paidAt,
            Money.of(10000),
            PaymentMethodType.WALLET
        );

        when(orderItemSnapshotRepository.getByTargetId(targetId)).thenReturn(itemSnapshot);
        when(orderSnapshotRepository.getById(orderId)).thenReturn(orderSnapshot);
        when(paymentSnapshotRepository.getByOrderNumber(orderNumber)).thenReturn(paymentSnapshot);
        when(feePolicyService.getPlatformFeeRate()).thenReturn(new BigDecimal("0.01"));
        when(feePolicyService.getPgFeeRate()).thenReturn(new BigDecimal("0.02"));

        settlementItemService.initializeSettlementItem(new InitializeSettlementItemCommand(targetId, confirmedAt));

        ArgumentCaptor<SettlementItem> captor = ArgumentCaptor.forClass(SettlementItem.class);
        verify(settlementItemRepository).save(captor.capture());
        SettlementItem saved = captor.getValue();

        assertThat(saved.getSellerId()).isEqualTo(sellerId);
        assertThat(saved.getType()).isEqualTo(SettlementItemType.ITEM_PAYMENT);
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getOrderItemId()).isEqualTo(orderItemId);
        assertThat(saved.getTargetId()).isEqualTo(targetId);
        assertThat(saved.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(saved.getOrderedAt()).isEqualTo(orderedAt);
        assertThat(saved.getPaidAt()).isEqualTo(paidAt);
        assertThat(saved.getConfirmedAt()).isEqualTo(confirmedAt);
        assertThat(saved.getLifeCycleMeta().getStatus()).isEqualTo(SettlementItemStatus.PENDING);
        assertThat(saved.getLifeCycleMeta().getExpectedDate())
            .isEqualTo(LocalDate.of(2024, 2, 1));

        SettlementCore core = saved.getCore();
        assertThat(core.paidAmount().amount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(core.platformFee().amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(core.pgFee().amount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(core.settlementAmount().amount()).isEqualByComparingTo(new BigDecimal("9700.00"));
    }

    @Test
    @DisplayName("정산 아이템 초기화 실패: 결제 완료 시점이 없으면 예외가 발생한다")
    void initializeSettlementItem_throwsWhenPaymentNotCompleted() {
        Long targetId = 10L;
        TargetType targetType = TargetType.FUNDING;
        OrderItemType orderItemType = OrderItemType.FUNDING_GIFT;
        Long orderItemId = 20L;
        Long orderId = 30L;
        Long sellerId = 40L;
        Long paymentId = 50L;
        String orderNumber = "ORD-20240101-001";

        LocalDateTime orderedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime confirmedAt = orderedAt.plusHours(2);

        OrderItemSnapshot itemSnapshot = new OrderItemSnapshot(
                orderItemId,
                orderId,
                targetId,
                targetType,
                orderItemType,
                sellerId,
                Money.of("5000"),
                Money.of("10000")
        );
        OrderSnapshot orderSnapshot = new OrderSnapshot(orderId, orderNumber, orderedAt);
        PaymentSnapshot paymentSnapshot = new PaymentSnapshot(
            paymentId,
            orderNumber,
            "payment-key",
            "transaction-key",
            null,
            Money.of(5000),
            PaymentMethodType.WALLET
        );

        when(orderItemSnapshotRepository.getByTargetId(targetId)).thenReturn(itemSnapshot);
        when(orderSnapshotRepository.getById(orderId)).thenReturn(orderSnapshot);
        when(paymentSnapshotRepository.getByOrderNumber(orderNumber)).thenReturn(paymentSnapshot);
        when(feePolicyService.getPlatformFeeRate()).thenReturn(new BigDecimal("0.01"));
        when(feePolicyService.getPgFeeRate()).thenReturn(new BigDecimal("0.02"));

        assertThatThrownBy(() ->
            settlementItemService.initializeSettlementItem(new InitializeSettlementItemCommand(targetId, confirmedAt))
        ).isInstanceOf(DomainException.class)
            .extracting(exception -> ((DomainException) exception).getErrorCode())
            .isEqualTo(SettlementErrorCode.PAYMENT_NOT_COMPLETED);
    }

    @Test
    @DisplayName("정산 아이템 초기화 실패: 구매 확정 시점이 없으면 예외가 발생한다")
    void initializeSettlementItem_throwsWhenConfirmedAtMissing() {
        Long targetId = 10L;
        TargetType targetType = TargetType.FUNDING;
        OrderItemType orderItemType = OrderItemType.FUNDING_GIFT;
        Long orderItemId = 20L;
        Long orderId = 30L;
        Long sellerId = 40L;
        Long paymentId = 50L;
        String orderNumber = "ORD-20240101-001";

        LocalDateTime orderedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime paidAt = orderedAt.plusHours(1);

        OrderItemSnapshot itemSnapshot = new OrderItemSnapshot(
                orderItemId,
                orderId,
                targetId,
                targetType,
                orderItemType,
                sellerId,
                Money.of("5000"),
                Money.of("10000")
        );
        OrderSnapshot orderSnapshot = new OrderSnapshot(orderId, orderNumber, orderedAt);
        PaymentSnapshot paymentSnapshot = new PaymentSnapshot(
            paymentId,
            orderNumber,
            "payment-key",
            "transaction-key",
            paidAt,
            Money.of(3000),
            PaymentMethodType.WALLET
        );

        when(orderItemSnapshotRepository.getByTargetId(targetId)).thenReturn(itemSnapshot);
        when(orderSnapshotRepository.getById(orderId)).thenReturn(orderSnapshot);
        when(paymentSnapshotRepository.getByOrderNumber(orderNumber)).thenReturn(paymentSnapshot);
        when(feePolicyService.getPlatformFeeRate()).thenReturn(new BigDecimal("0.01"));
        when(feePolicyService.getPgFeeRate()).thenReturn(new BigDecimal("0.02"));

        assertThatThrownBy(() ->
            settlementItemService.initializeSettlementItem(new InitializeSettlementItemCommand(targetId, null))
        ).isInstanceOf(DomainException.class)
            .extracting(exception -> ((DomainException) exception).getErrorCode())
            .isEqualTo(SettlementErrorCode.CONFIRMED_AT_REQUIRED);
    }
}
