package app.giftify.settlement.application.service;

import app.giftify.settlement.application.inbound.CancelSettlementCommand;
import app.giftify.settlement.application.inbound.CreateSettlementCommand;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.application.service.dto.SettlementSummary;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementItemType;
import app.giftify.settlement.domain.service.FeePolicyService;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.settlement.domain.projection.AmountSummaryProjection;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        @DisplayName("비관적 락으로 정산 아이템을 조회해 cancel()을 호출한다")
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
            verify(settlementItemRepository).getByOrderIdAndOrderItemIdAndTypeWithLock(
                    orderId, orderItemId, SettlementItemType.ITEM_PAYMENT);
            verify(mockItem).cancel();
        }

        @Test
        @DisplayName("정산 아이템이 존재하지 않으면 DomainException이 전파된다")
        void given_notExistingItem_when_cancel_then_throwDomainException() {
            // given
            Long orderId = 10L;
            Long orderItemId = 20L;

            when(settlementItemRepository.getByOrderIdAndOrderItemIdAndTypeWithLock(
                    orderId, orderItemId, SettlementItemType.ITEM_PAYMENT))
                    .thenThrow(new DomainException(SettlementErrorCode.SETTLEMENT_ITEM_NOT_FOUND));

            // when & then
            assertThatThrownBy(() ->
                    settlementItemService.cancel(new CancelSettlementCommand(orderId, orderItemId)))
                    .isInstanceOf(DomainException.class)
                    .extracting(e -> ((DomainException) e).getErrorCode())
                    .isEqualTo(SettlementErrorCode.SETTLEMENT_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("summarizeSettlements()")
    class SummarizeSettlementsTests {

        @Test
        @DisplayName("sellerId와 pageable로 조회한 정산 요약을 그대로 반환한다")
        void given_sellerId_when_summarizeSettlements_then_returnPage() {
            // given
            Long sellerId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            SettlementSummary summary = new SettlementSummary(
                    "2024-03", 10L, 10000L, 9700L, 2L, "CREATED"
            );
            Page<SettlementSummary> expectedPage = new PageImpl<>(List.of(summary), pageable, 1L);

            when(settlementItemRepository.getSettlementSummary(sellerId, pageable))
                    .thenReturn(expectedPage);

            // when
            Page<SettlementSummary> result = settlementItemService.summarizeSettlements(sellerId, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).settlementMonth()).isEqualTo("2024-03");
            assertThat(result.getContent().get(0).totalSalesAmount()).isEqualTo(10000L);
            assertThat(result.getContent().get(0).totalSettlementAmount()).isEqualTo(9700L);
            verify(settlementItemRepository).getSettlementSummary(sellerId, pageable);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 Page를 반환한다")
        void given_noResults_when_summarizeSettlements_then_returnEmptyPage() {
            // given
            Long sellerId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<SettlementSummary> emptyPage = new PageImpl<>(List.of(), pageable, 0L);

            when(settlementItemRepository.getSettlementSummary(sellerId, pageable))
                    .thenReturn(emptyPage);

            // when
            Page<SettlementSummary> result = settlementItemService.summarizeSettlements(sellerId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0L);
        }
    }
}