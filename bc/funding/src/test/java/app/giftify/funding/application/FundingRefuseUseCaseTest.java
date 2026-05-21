package app.giftify.funding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;

@ExtendWith(MockitoExtension.class)
class FundingRefuseUseCaseTest {

	@InjectMocks
	private FundingRefuseUseCase fundingRefuseUseCase;

	@Mock
	private FundingRepository fundingRepository;
	@Mock
	private EventPublisher eventPublisher;
	@Mock
	private FundingParticipantMemberRepository fundingParticipantMemberRepository;

	private final Long fundingId = 10L;
	private final Long wishlistItemId = 200L;
	private final Long receiverId = 100L;

	@Test
	@DisplayName("펀딩 거절 성공: receiver 일치 시 refuse() 호출 + FundingCanceledEvent 발행")
	void refuse_Success() {
		Funding funding = mock(Funding.class);
		given(funding.getId()).willReturn(fundingId);
		given(funding.getReceiverId()).willReturn(receiverId);
		given(funding.getWishlistItemId()).willReturn(wishlistItemId);
		given(funding.getCurrentAmount()).willReturn(3000);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(fundingParticipantMemberRepository.findIdsByFundingId(fundingId))
				.willReturn(List.of(11L, 12L));

		fundingRefuseUseCase.refuseFunding(fundingId, receiverId);

		then(funding).should().refuse();
		ArgumentCaptor<FundingCanceledEvent> captor = ArgumentCaptor.forClass(FundingCanceledEvent.class);
		then(eventPublisher).should().publish(captor.capture());
		FundingCanceledEvent event = captor.getValue();
		assertThat(event.getFundingId()).isEqualTo(fundingId);
		assertThat(event.getWishlistItemId()).isEqualTo(wishlistItemId);
		assertThat(event.getCanceledAmount()).isEqualTo(3000);
		assertThat(event.getReceiverId()).isEqualTo(receiverId);
		assertThat(event.getParticipantIds()).containsExactly(11L, 12L);
	}

	@Test
	@DisplayName("펀딩 거절 실패: receiver 가 일치하지 않으면 FORBIDDEN 예외")
	void refuse_Fail_NotReceiver() {
		Funding funding = mock(Funding.class);
		given(funding.getReceiverId()).willReturn(receiverId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

		assertThatThrownBy(() -> fundingRefuseUseCase.refuseFunding(fundingId, 999L))
				.isInstanceOf(FundingException.class);

		then(funding).should(never()).refuse();
		then(eventPublisher).should(never()).publish(any(FundingCanceledEvent.class));
	}

	@Test
	@DisplayName("펀딩 거절 실패: 펀딩 미존재 시 FUNDING_NOT_FOUND 예외")
	void refuse_Fail_FundingNotFound() {
		given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingRefuseUseCase.refuseFunding(fundingId, receiverId))
				.isInstanceOf(FundingException.class);

		then(fundingParticipantMemberRepository).should(never()).findIdsByFundingId(anyLong());
	}
}
