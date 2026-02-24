package app.giftify.settlement.application.service;

import app.giftify.settlement.application.inbound.CancelSettlementCommand;
import app.giftify.settlement.application.inbound.CreateSettlementCommand;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementItemType;
import app.giftify.settlement.domain.service.FeePolicyService;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.api.AmountSummaryProjection;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementItemService 테스트")
class SettlementItemServiceTest {

    @Mock
    private SettlementItemRepository settlementItemRepository;

    @Mock
    private FeePolicyService feePolicyService;

    @InjectMocks
    private SettlementItemService settlementItemService;

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("중복이 없으면 정산 아이템을 생성해 저장한다")
        void given_validSnapshot_when_create_then_saveSettlementItem() {
            // given
            LocalDateTime confirmedAt = LocalDateTime.of(2024, 3, 15, 12, 0);
            OrderItemSnapshot snapshot = new OrderItemSnapshot(
                    10L, 20L, 30L, 40L,
                    TargetType.FUNDING,
                    50L,
                    Money.of("10000"),
                    confirmedAt
            );

            when(settlementItemRepository.existsByOrderItemIdAndType(20L, SettlementItemType.ITEM_PAYMENT))
                    .thenReturn(false);
            when(feePolicyService.getPlatformFeeRate()).thenReturn(new BigDecimal("0.01"));
            when(feePolicyService.getPgFeeRate()).thenReturn(new BigDecimal("0.02"));

            // when
            settlementItemService.create(new CreateSettlementCommand(snapshot));

            // then
            ArgumentCaptor<SettlementItem> captor = ArgumentCaptor.forClass(SettlementItem.class);
            verify(settlementItemRepository).save(captor.capture());

            SettlementItem saved = captor.getValue();
            assertThat(saved.getSellerId()).isEqualTo(30L);
            assertThat(saved.getOrderId()).isEqualTo(10L);
            assertThat(saved.getOrderItemId()).isEqualTo(20L);
            assertThat(saved.getTargetId()).isEqualTo(40L);
            assertThat(saved.getTargetType()).isEqualTo(TargetType.FUNDING);
            assertThat(saved.getPaymentId()).isEqualTo(50L);
            assertThat(saved.getType()).isEqualTo(SettlementItemType.ITEM_PAYMENT);
            assertThat(saved.getConfirmedAt()).isEqualTo(confirmedAt);
            assertThat(saved.getStatusInfo().getStatus()).isEqualTo(SettlementItemStatus.CREATED);
            assertThat(saved.getStatusInfo().getExpectedDate()).isEqualTo(LocalDate.of(2024, 4, 1));
            assertThat(saved.getCore().paidAmount().amount()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(saved.getCore().platformFee().amount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(saved.getCore().pgFee().amount()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(saved.getCore().settlementAmount().amount()).isEqualByComparingTo(new BigDecimal("9700.00"));
        }

        @Test
        @DisplayName("동일한 주문 아이템+유형의 정산 아이템이 이미 존재하면 저장을 스킵한다")
        void given_duplicateSnapshot_when_create_then_skipSave() {
            // given
            OrderItemSnapshot snapshot = new OrderItemSnapshot(
                    10L, 20L, 30L, 40L,
                    TargetType.FUNDING,
                    50L,
                    Money.of("10000"),
                    LocalDateTime.of(2024, 3, 15, 12, 0)
            );

            when(settlementItemRepository.existsByOrderItemIdAndType(20L, SettlementItemType.ITEM_PAYMENT))
                    .thenReturn(true);

            // when
            settlementItemService.create(new CreateSettlementCommand(snapshot));

            // then
            verify(settlementItemRepository, never()).save(any());
            verify(feePolicyService, never()).getPlatformFeeRate();
            verify(feePolicyService, never()).getPgFeeRate();
        }
    }

    @Nested
    @DisplayName("getTotalAmounts()")
    class GetTotalAmountsTests {

        @Test
        @DisplayName("orderIds에 해당하는 정산 합산 금액을 Map으로 반환한다")
        void given_orderIds_when_getTotalAmounts_then_returnMoneyMap() {
            // given
            List<Long> orderIds = List.of(1L, 2L);
            List<AmountSummaryProjection> projections = List.of(
                    new AmountSummaryProjection(1L, new BigDecimal("10000")),
                    new AmountSummaryProjection(2L, new BigDecimal("20000"))
            );
            when(settlementItemRepository.getSettlementSumByOrderIds(orderIds)).thenReturn(projections);

            // when
            Map<Long, Money> result = settlementItemService.getTotalAmounts(orderIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(1L).amount()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(result.get(2L).amount()).isEqualByComparingTo(new BigDecimal("20000"));
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 Map을 반환한다")
        void given_noResults_when_getTotalAmounts_then_returnEmptyMap() {
            // given
            List<Long> orderIds = List.of(99L);
            when(settlementItemRepository.getSettlementSumByOrderIds(orderIds)).thenReturn(List.of());

            // when
            Map<Long, Money> result = settlementItemService.getTotalAmounts(orderIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("정산 아이템을 조회해 cancel()을 호출한다")
        void given_validCommand_when_cancel_then_callCancel() {
            // given
            Long orderId = 10L;
            Long orderItemId = 20L;
            SettlementItem mockItem = mock(SettlementItem.class);

            when(settlementItemRepository.getByOrderIdAndOrderItemIdAndTypeWithLock(
                    orderId, orderItemId, SettlementItemType.ITEM_PAYMENT))
                    .thenReturn(mockItem);

            // when
            settlementItemService.cancel(new CancelSettlementCommand(orderId, orderItemId));

            // then
            verify(mockItem).cancel();
        }
    }
}