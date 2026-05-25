package app.giftify.funding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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
import app.giftify.support.common.event.EventPublisher;
import app.giftify.funding.domain.event.FundingFailAcceptEvent;

@ExtendWith(MockitoExtension.class)
class FundingFailAcceptUseCaseTest {

	@InjectMocks
	private FundingFailAcceptUseCase fundingFailAcceptUseCase;

	@Mock
	private FundingRepository fundingRepository;
	@Mock
	private EventPublisher eventPublisher;

	private final Long fundingId = 10L;
	private final Long receiverId = 100L;

	@Test
	@DisplayName("수락 실패 처리 성공: markAcceptFailed() 호출 + FundingFailAcceptEvent 발행")
	void execute_Success() {
		Funding funding = mock(Funding.class);
		given(funding.getId()).willReturn(fundingId);
		given(funding.getReceiverId()).willReturn(receiverId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

		fundingFailAcceptUseCase.execute(fundingId);

		then(funding).should().markAcceptFailed();
		ArgumentCaptor<FundingFailAcceptEvent> captor =
				ArgumentCaptor.forClass(FundingFailAcceptEvent.class);
		then(eventPublisher).should().publish(captor.capture());
		assertThat(captor.getValue().fundingId()).isEqualTo(fundingId);
		assertThat(captor.getValue().receiverId()).isEqualTo(receiverId);
	}

	@Test
	@DisplayName("수락 실패 처리 실패: 펀딩 미존재 시 FUNDING_NOT_FOUND 예외")
	void execute_Fail_FundingNotFound() {
		given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingFailAcceptUseCase.execute(fundingId))
				.isInstanceOf(FundingException.class);

		then(eventPublisher).should(never()).publish(any(FundingFailAcceptEvent.class));
	}
}
