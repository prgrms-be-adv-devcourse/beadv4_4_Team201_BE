package app.giftify.funding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.type.FundingStatus;

@ExtendWith(MockitoExtension.class)
class FundingCloseUseCaseTest {

	@InjectMocks
	private FundingCloseUseCase fundingCloseUseCase;

	@Mock
	private FundingRepository fundingRepository;
	@Mock
	private EventPublisher eventPublisher;
	@Mock
	private FundingParticipantMemberRepository fundingParticipantMemberRepository;

	private final Long fundingId = 10L;
	private final Long receiverId = 100L;
	private final Long wishlistItemId = 200L;

	private Funding closableFundingStub() {
		Funding funding = mock(Funding.class);
		lenient().when(funding.getId()).thenReturn(fundingId);
		lenient().when(funding.getReceiverId()).thenReturn(receiverId);
		lenient().when(funding.getWishlistItemId()).thenReturn(wishlistItemId);
		lenient().when(funding.getCurrentAmount()).thenReturn(5000);
		lenient().when(funding.getStatus()).thenReturn(FundingStatus.IN_PROGRESS);
		lenient().when(funding.getProductName()).thenReturn("상품명");
		lenient().when(funding.getClosedAt()).thenReturn(null);
		lenient().when(funding.isExpired()).thenReturn(false);
		return funding;
	}

	@Test
	@DisplayName("closeFunding 성공: close() 호출 + FundingCanceledEvent 발행 + DTO 반환")
	void closeFunding_Success() {
		Funding funding = closableFundingStub();
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(fundingParticipantMemberRepository.findIdsByFundingId(fundingId))
				.willReturn(List.of(11L, 12L));

		FundingCompleteResponseDto dto = fundingCloseUseCase.closeFunding(fundingId);

		then(funding).should().close();
		then(eventPublisher).should().publish(any(FundingCanceledEvent.class));
		assertThat(dto.fundingId()).isEqualTo(fundingId);
	}

	@Test
	@DisplayName("closeFunding 실패: 이미 CLOSED 상태이면 ALREADY_TERMINATED")
	void closeFunding_Fail_AlreadyClosed() {
		Funding funding = closableFundingStub();
		given(funding.getStatus()).willReturn(FundingStatus.CLOSED);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

		assertThatThrownBy(() -> fundingCloseUseCase.closeFunding(fundingId))
				.isInstanceOf(FundingException.class);

		then(funding).should(never()).close();
		then(eventPublisher).should(never()).publish(any(FundingCanceledEvent.class));
	}

	@Test
	@DisplayName("closeFunding 실패: 만료된 펀딩이면 ALREADY_TERMINATED")
	void closeFunding_Fail_Expired() {
		Funding funding = closableFundingStub();
		given(funding.isExpired()).willReturn(true);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));

		assertThatThrownBy(() -> fundingCloseUseCase.closeFunding(fundingId))
				.isInstanceOf(FundingException.class);

		then(funding).should(never()).close();
	}

	@Test
	@DisplayName("closeFunding 실패: 펀딩 미존재 FUNDING_NOT_FOUND")
	void closeFunding_Fail_NotFound() {
		given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingCloseUseCase.closeFunding(fundingId))
				.isInstanceOf(FundingException.class);

		then(eventPublisher).should(never()).publish(any(FundingCanceledEvent.class));
	}

	@Test
	@DisplayName("closeUnacceptedAchievedFundings 성공: 2주 경과 ACHIEVED 펀딩 일괄 종료 + 이벤트 N 회 발행")
	void closeUnacceptedAchievedFundings_Success() {
		Funding f1 = closableFundingStub();
		Funding f2 = closableFundingStub();
		given(fundingRepository.findByStatusAndAchievedAtBefore(
				any(FundingStatus.class), any(LocalDateTime.class)))
				.willReturn(List.of(f1, f2));

		List<FundingCompleteResponseDto> results = fundingCloseUseCase.closeUnacceptedAchievedFundings();

		then(f1).should().close();
		then(f2).should().close();
		then(eventPublisher).should(times(2)).publish(any(FundingCanceledEvent.class));
		assertThat(results).hasSize(2);
	}

	@Test
	@DisplayName("closeUnacceptedAchievedFundings 성공: 대상 펀딩 없으면 빈 리스트 반환, 이벤트 미발행")
	void closeUnacceptedAchievedFundings_Empty() {
		given(fundingRepository.findByStatusAndAchievedAtBefore(
				any(FundingStatus.class), any(LocalDateTime.class)))
				.willReturn(List.of());

		List<FundingCompleteResponseDto> results = fundingCloseUseCase.closeUnacceptedAchievedFundings();

		assertThat(results).isEmpty();
		then(eventPublisher).should(never()).publish(any(FundingCanceledEvent.class));
	}
}
