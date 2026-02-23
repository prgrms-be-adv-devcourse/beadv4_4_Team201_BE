package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.FundingCreateResult;
import app.giftify.funding.adpater.inbound.dto.FundingContributeRequest;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.order.domain.OrderItemSnapshot;
import app.giftify.order.domain.OrderSnapshot;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingFacadeTest {

    @InjectMocks
    private FundingFacade fundingFacade;

    @Mock
    private FundingCreateUseCase fundingCreateUseCase;

    @Mock
    private FundingContributeUseCase fundingContributeUseCase;

    @Test
    @DisplayName("processFundingActions 성공: 펀딩 생성 후 즉시 기여")
    void processFundingActions_CreateAndContribute() {
        // given
        Long buyerId = 1L;
        Long fundingId = 100L;
        Integer amount = 10000;

        // 1. OrderItemSnapshot 생성 (TargetType.FUNDING_PENDING)
        OrderItemSnapshot orderItemSnapshot = OrderItemSnapshot.builder()
                .targetType(TargetType.FUNDING_PENDING)
                .targetId(10L) // wishlistItemId
                .amount(new Money(BigDecimal.valueOf(amount)))
                .build();

        OrderSnapshot orderSnapshot = OrderSnapshot.builder()
                .buyerId(buyerId)
                .orderItemSnapshots(List.of(orderItemSnapshot))
                .build();

        // 2. Mock Funding 객체 생성
        Funding mockFunding = mock(Funding.class);
        when(mockFunding.getId()).thenReturn(fundingId);

        // 3. FundingCreateResult 생성
        FundingCreateResult createResult = new FundingCreateResult(mockFunding, orderItemSnapshot);

        // 4. createFunding 호출 시 결과 반환 설정
        when(fundingCreateUseCase.createFunding(any(OrderSnapshot.class)))
                .thenReturn(List.of(createResult));

        // when
        fundingFacade.processFundingActions(orderSnapshot);

        // then
        // 1. createFunding이 호출되었는지 검증
        verify(fundingCreateUseCase).createFunding(orderSnapshot);

        // 2. contribute가 생성된 펀딩 ID와 금액으로 호출되었는지 검증
        ArgumentCaptor<List<FundingContributeRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(fundingContributeUseCase).contribute(captor.capture(), eq(buyerId));

        List<FundingContributeRequest> capturedRequests = captor.getValue();
        assertThat(capturedRequests).hasSize(1);
        assertThat(capturedRequests.get(0).fundingId()).isEqualTo(fundingId);
        assertThat(capturedRequests.get(0).amount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("processFundingActions 성공: 기존 펀딩 기여")
    void processFundingActions_ContributeOnly() {
        // given
        Long buyerId = 1L;
        Long fundingId = 200L;
        Integer amount = 5000;

        // 1. OrderItemSnapshot 생성 (TargetType.FUNDING)
        OrderItemSnapshot orderItemSnapshot = OrderItemSnapshot.builder()
                .targetType(TargetType.FUNDING)
                .targetId(fundingId) // fundingId
                .amount(new Money(BigDecimal.valueOf(amount)))
                .build();

        OrderSnapshot orderSnapshot = OrderSnapshot.builder()
                .buyerId(buyerId)
                .orderItemSnapshots(List.of(orderItemSnapshot))
                .build();

        // when
        fundingFacade.processFundingActions(orderSnapshot);

        // then
        // 1. createFunding은 호출되지 않아야 함
        verify(fundingCreateUseCase, never()).createFunding(any());

        // 2. contribute가 호출되었는지 검증
        ArgumentCaptor<List<FundingContributeRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(fundingContributeUseCase).contribute(captor.capture(), eq(buyerId));

        List<FundingContributeRequest> capturedRequests = captor.getValue();
        assertThat(capturedRequests).hasSize(1);
        assertThat(capturedRequests.get(0).fundingId()).isEqualTo(fundingId);
        assertThat(capturedRequests.get(0).amount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("processFundingActions 성공: 펀딩 생성과 기존 펀딩 기여 동시 처리")
    void processFundingActions_CreateAndContributeMixed() {
        // given
        Long buyerId = 1L;
        Long newFundingId = 100L;
        long existingFundingId = 200L;
        int newAmount = 10000;
        int existingAmount = 5000;

        // 1. OrderItemSnapshot 생성 (FUNDING_PENDING)
        OrderItemSnapshot newFundingItem = OrderItemSnapshot.builder()
                .targetType(TargetType.FUNDING_PENDING)
                .targetId(10L)
                .amount(new Money(BigDecimal.valueOf(newAmount)))
                .build();

        // 2. OrderItemSnapshot 생성 (FUNDING)
        OrderItemSnapshot existingFundingItem = OrderItemSnapshot.builder()
                .targetType(TargetType.FUNDING)
                .targetId(existingFundingId)
                .amount(new Money(BigDecimal.valueOf(existingAmount)))
                .build();

        OrderSnapshot orderSnapshot = OrderSnapshot.builder()
                .buyerId(buyerId)
                .orderItemSnapshots(List.of(newFundingItem, existingFundingItem))
                .build();

        // 3. Mock Funding 객체 생성
        Funding mockFunding = mock(Funding.class);
        when(mockFunding.getId()).thenReturn(newFundingId);

        // 4. FundingCreateResult 생성
        FundingCreateResult createResult = new FundingCreateResult(mockFunding, newFundingItem);

        // 5. createFunding 호출 시 결과 반환 설정
        when(fundingCreateUseCase.createFunding(any(OrderSnapshot.class)))
                .thenReturn(List.of(createResult));

        // when
        fundingFacade.processFundingActions(orderSnapshot);

        // then
        // 1. createFunding이 호출되었는지 검증
        verify(fundingCreateUseCase).createFunding(orderSnapshot);

        // 2. contribute가 두 건 모두 포함하여 호출되었는지 검증
        ArgumentCaptor<List<FundingContributeRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(fundingContributeUseCase).contribute(captor.capture(), eq(buyerId));

        List<FundingContributeRequest> capturedRequests = captor.getValue();
        assertThat(capturedRequests).hasSize(2);
        
        // 순서는 보장되지 않을 수 있으므로 포함 여부로 검증하거나 정렬 후 검증
        assertThat(capturedRequests).extracting("fundingId")
                .containsExactlyInAnyOrder(newFundingId, existingFundingId);
    }
}
