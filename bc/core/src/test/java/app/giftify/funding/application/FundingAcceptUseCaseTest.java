package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingConfirmPendingEvent;
import app.giftify.shared.domain.type.FundingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingAcceptUseCaseTest {

    @InjectMocks
    private FundingAcceptUseCase fundingAcceptUseCase;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("펀딩 수락 요청 성공: 상태가 ACCEPTANCE_PENDING으로 변경되고 이벤트가 발행된다")
    void requestFundingAcceptance_Success() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        Integer targetAmount = 50000;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        ReflectionTestUtils.setField(funding, "id", fundingId);
        // 달성 상태로 만들기
        funding.contribute(targetAmount);

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when
        fundingAcceptUseCase.requestFundingAcceptance(fundingId, memberId);

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACCEPTING);
        verify(eventPublisher, times(1)).publish(any(FundingConfirmPendingEvent.class));
    }

    @Test
    @DisplayName("펀딩 수락 요청 실패: 이미 수락 요청 중인 상태")
    void requestFundingAcceptance_Fail_AlreadyPending() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        Integer targetAmount = 50000;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        ReflectionTestUtils.setField(funding, "id", fundingId);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.ACCEPTING); // 이미 대기 상태

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when & then
        // Funding.pendingAcceptance()에서 예외를 던져야 함
        assertThatThrownBy(() -> fundingAcceptUseCase.requestFundingAcceptance(fundingId, memberId))
                .isInstanceOf(FundingException.class);
    }

    @Test
    @DisplayName("펀딩 수락 요청 실패: 펀딩이 존재하지 않음")
    void requestFundingAcceptance_NotFound() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.requestFundingAcceptance(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.FUNDING_NOT_FOUND);
    }

    @Test
    @DisplayName("펀딩 수락 요청 실패: 수신자가 아님")
    void requestFundingAcceptance_Forbidden() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        Long otherId = 999L;
        Integer targetAmount = 50000;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.requestFundingAcceptance(fundingId, otherId))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("펀딩 수락 요청 실패: 목표 달성 안됨")
    void requestFundingAcceptance_NotAchieved() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        Integer targetAmount = 50000;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        // 기여 없음 -> IN_PROGRESS 상태
        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.requestFundingAcceptance(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.NOT_ACHIEVED);
    }

    @Test
    @DisplayName("펀딩 수락 요청 실패: 이미 최종 결정됨 (ACCEPTED)")
    void requestFundingAcceptance_AlreadyDecided() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        Integer targetAmount = 50000;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        ReflectionTestUtils.setField(funding, "id", fundingId);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.ACCEPTED); // 이미 수락됨

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.requestFundingAcceptance(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.ALREADY_DECIDED);
    }

    @Test
    @DisplayName("펀딩 수락 확정 성공: 상태가 ACCEPTED로 변경됨")
    void confirmFundingAcceptance_Success() {
        // given
        Long fundingId = 1L;
        Integer targetAmount = 50000;
        Long memberId = 100L;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.ACCEPTING); // 수락 대기 상태

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when
        fundingAcceptUseCase.confirmFundingAcceptance(fundingId);

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACCEPTED);
        assertThat(funding.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("펀딩 수락 확정 실패: 이미 ACCEPTED 상태")
    void confirmFundingAcceptance_Fail_AlreadyAccepted() {
        // given
        Long fundingId = 1L;
        Integer targetAmount = 50000;
        Long memberId = 100L;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.ACCEPTED); // 이미 수락됨

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when & then
        // Funding.confirmAcceptance()에서 예외를 던져야 함
        assertThatThrownBy(() -> fundingAcceptUseCase.confirmFundingAcceptance(fundingId))
                .isInstanceOf(FundingException.class);
    }

    @Test
    @DisplayName("펀딩 수락 확정 실패: 상태가 ACCEPTANCE_PENDING이 아님 (예: IN_PROGRESS)")
    void confirmFundingAcceptance_InvalidStatus() {
        // given
        Long fundingId = 1L;
        Integer targetAmount = 50000;
        Long memberId = 100L;

        Funding funding = Funding.startFunding(1L, 10L, "Product", "img", memberId, targetAmount);
        // IN_PROGRESS 상태

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.confirmFundingAcceptance(fundingId))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.INVALID_STATUS_FOR_ACCEPTANCE_PENDING);
    }
}
