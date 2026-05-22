package app.giftify.funding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingConfirmPendingEvent;

@ExtendWith(MockitoExtension.class)
class FundingRetryAcceptUseCaseTest {

	@InjectMocks
	private FundingRetryAcceptUseCase fundingRetryAcceptUseCase;

	@Mock
	private FundingRepository fundingRepository;
	@Mock
	private EventPublisher eventPublisher;

	private final Long fundingId = 10L;
	private final Long memberId = 100L;
	private final Long productId = 555L;

	@Test
	@DisplayName("재수락 성공: 수락 가능 상태 시 pendingAcceptance() + FundingConfirmPendingEvent 발행")
	void retryAccept_Success() {
		Funding funding = mock(Funding.class);
		given(funding.getId()).willReturn(fundingId);
		given(funding.getProductId()).willReturn(productId);
		given(funding.canRetryAccept(any(LocalDateTime.class))).willReturn(true);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

		fundingRetryAcceptUseCase.retryAccept(fundingId, memberId);

		then(funding).should().validateReceiver(memberId);
		then(funding).should().pendingAcceptance();
		ArgumentCaptor<FundingConfirmPendingEvent> captor =
				ArgumentCaptor.forClass(FundingConfirmPendingEvent.class);
		then(eventPublisher).should().publish(captor.capture());
		assertThat(captor.getValue().getFundingId()).isEqualTo(fundingId);
		assertThat(captor.getValue().getProductId()).isEqualTo(productId);
	}

	@Test
	@DisplayName("재수락 실패: canRetryAccept false 시 INVALID_STATUS 예외, pendingAcceptance 미호출")
	void retryAccept_Fail_InvalidStatus() {
		Funding funding = mock(Funding.class);
		given(funding.canRetryAccept(any(LocalDateTime.class))).willReturn(false);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

		assertThatThrownBy(() -> fundingRetryAcceptUseCase.retryAccept(fundingId, memberId))
				.isInstanceOf(FundingException.class);

		then(funding).should().validateReceiver(memberId);
		then(funding).should(never()).pendingAcceptance();
		then(eventPublisher).should(never()).publish(any(FundingConfirmPendingEvent.class));
	}

	@Test
	@DisplayName("재수락 실패: receiver 불일치 시 validateReceiver 에서 예외")
	void retryAccept_Fail_NotReceiver() {
		Funding funding = mock(Funding.class);
		doThrow(new FundingException(app.giftify.funding.domain.exception.FundingErrorCode.FORBIDDEN))
				.when(funding).validateReceiver(memberId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

		assertThatThrownBy(() -> fundingRetryAcceptUseCase.retryAccept(fundingId, memberId))
				.isInstanceOf(FundingException.class);

		then(funding).should(never()).canRetryAccept(any(LocalDateTime.class));
		then(funding).should(never()).pendingAcceptance();
	}

	@Test
	@DisplayName("재수락 실패: 펀딩 미존재 시 FUNDING_NOT_FOUND 예외")
	void retryAccept_Fail_FundingNotFound() {
		given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingRetryAcceptUseCase.retryAccept(fundingId, memberId))
				.isInstanceOf(FundingException.class);
	}
}
