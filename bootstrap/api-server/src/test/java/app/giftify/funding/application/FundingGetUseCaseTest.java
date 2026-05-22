package app.giftify.funding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import app.giftify.funding.adapter.inbound.dto.ContributeFundingResponseDto;
import app.giftify.funding.adapter.inbound.dto.FundingResponseDto;
import app.giftify.funding.adapter.inbound.dto.MyFundingResponseDto;
import app.giftify.funding.adapter.inbound.dto.MyFundingSummaryDto;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.jpa.FundingParticipantMember;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.funding.readmodel.MemberView;
import app.giftify.funding.readmodel.MemberViewRepository;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.port.FriendshipVerificationPort;
import app.giftify.shared.domain.type.FundingStatus;

@ExtendWith(MockitoExtension.class)
class FundingGetUseCaseTest {

	@InjectMocks
	private FundingGetUseCase fundingGetUseCase;

	@Mock
	private FundingRepository fundingRepository;
	@Mock
	private FundingParticipantMemberRepository participantMemberRepository;
	@Mock
	private MemberViewRepository memberViewRepository;
	@Mock
	private FriendshipVerificationPort friendshipVerificationPort;

	private final Long fundingId = 10L;
	private final Long memberId = 100L;
	private final Long receiverId = 200L;
	private final Long friendId = 300L;
	private final Long productId = 555L;
	private final Long wishlistItemId = 777L;
	private final String nickname = "수령자닉";

	private Funding fundingStub(FundingStatus status) {
		Funding funding = mock(Funding.class);
		lenient().when(funding.getId()).thenReturn(fundingId);
		lenient().when(funding.getReceiverId()).thenReturn(receiverId);
		lenient().when(funding.getStatus()).thenReturn(status);
		lenient().when(funding.getTargetAmount()).thenReturn(10000);
		lenient().when(funding.getCurrentAmount()).thenReturn(3000);
		lenient().when(funding.getDeadline()).thenReturn(LocalDateTime.now().plusDays(5));
		lenient().when(funding.getWishlistItemId()).thenReturn(wishlistItemId);
		lenient().when(funding.getProductId()).thenReturn(productId);
		lenient().when(funding.getProductName()).thenReturn("상품명");
		lenient().when(funding.getImageKey()).thenReturn("img.key");
		lenient().when(funding.getClosedAt()).thenReturn(null);
		return funding;
	}

	private MemberView receiverStub(Long id) {
		MemberView view = mock(MemberView.class);
		lenient().when(view.getId()).thenReturn(id);
		lenient().when(view.getNickname()).thenReturn(nickname);
		return view;
	}

	// ---------- getFunding (단건 전체 공개) ----------

	@Test
	@DisplayName("getFunding 성공: IN_PROGRESS 상태일 때 receiver 닉네임 포함된 DTO 반환")
	void getFunding_Success() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView receiver = receiverStub(receiverId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(memberViewRepository.findById(receiverId)).willReturn(Optional.of(receiver));

		FundingResponseDto dto = fundingGetUseCase.getFunding(fundingId);

		assertThat(dto.fundingId()).isEqualTo(fundingId);
		assertThat(dto.receiverNickname()).isEqualTo(nickname);
		assertThat(dto.status()).isEqualTo(FundingStatus.IN_PROGRESS);
	}

	@Test
	@DisplayName("getFunding 실패: 펀딩 미존재 시 FUNDING_NOT_FOUND")
	void getFunding_Fail_NotFound() {
		given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
				.isInstanceOf(FundingException.class);
	}

	@Test
	@DisplayName("getFunding 실패: receiver MemberView 미존재 시 RECEIVER_NOT_FOUND")
	void getFunding_Fail_ReceiverNotFound() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(memberViewRepository.findById(receiverId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
				.isInstanceOf(FundingException.class);
	}

	@Test
	@DisplayName("getFunding 실패: 종료 상태(CLOSED 등)일 때 NOT_IN_PROGRESS")
	void getFunding_Fail_NotInProgress() {
		Funding funding = fundingStub(FundingStatus.CLOSED);
		MemberView receiver = receiverStub(receiverId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(memberViewRepository.findById(receiverId)).willReturn(Optional.of(receiver));

		assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
				.isInstanceOf(FundingException.class);
	}

	// ---------- getFundings (리스트 전체 공개) ----------

	@Test
	@DisplayName("getFundings 성공: receiver 닉네임 매핑된 페이지 응답 반환")
	void getFundings_Success() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView receiver = receiverStub(receiverId);
		Page<Funding> page = new PageImpl<>(List.of(funding));
		given(fundingRepository.findAllByStatusIn(any(), any(Pageable.class))).willReturn(page);
		given(memberViewRepository.findAllById(anySet())).willReturn(List.of(receiver));

		PageResponse<FundingResponseDto> result = fundingGetUseCase.getFundings(0, 10);

		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).receiverNickname()).isEqualTo(nickname);
	}

	@Test
	@DisplayName("getFundings 성공: receiver 매핑 누락 시 '알 수 없음' 폴백")
	void getFundings_Success_UnknownReceiver() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		Page<Funding> page = new PageImpl<>(List.of(funding));
		given(fundingRepository.findAllByStatusIn(any(), any(Pageable.class))).willReturn(page);
		given(memberViewRepository.findAllById(anySet())).willReturn(Collections.emptyList());

		PageResponse<FundingResponseDto> result = fundingGetUseCase.getFundings(0, 10);

		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).receiverNickname()).isEqualTo("알 수 없음");
	}

	// ---------- getParticipatedFunding (참여 단건) ----------

	@Test
	@DisplayName("getParticipatedFunding 성공: 참여자이고 기여금 존재 시 DTO 반환")
	void getParticipatedFunding_Success() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView receiver = receiverStub(receiverId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(memberViewRepository.findById(receiverId)).willReturn(Optional.of(receiver));
		given(participantMemberRepository.existsByFundingIdAndParticipantId(fundingId, memberId)).willReturn(true);
		given(participantMemberRepository.findTotalAmountByFundingIdAndParticipantId(fundingId, memberId))
				.willReturn(Optional.of(2500));

		ContributeFundingResponseDto dto = fundingGetUseCase.getParticipatedFunding(fundingId, memberId);

		assertThat(dto.fundingId()).isEqualTo(fundingId);
		assertThat(dto.myContribution()).isEqualTo(2500);
	}

	@Test
	@DisplayName("getParticipatedFunding 성공: 기여금 미존재 시 0 으로 폴백")
	void getParticipatedFunding_Success_ZeroContribution() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView receiver = receiverStub(receiverId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(memberViewRepository.findById(receiverId)).willReturn(Optional.of(receiver));
		given(participantMemberRepository.existsByFundingIdAndParticipantId(fundingId, memberId)).willReturn(true);
		given(participantMemberRepository.findTotalAmountByFundingIdAndParticipantId(fundingId, memberId))
				.willReturn(Optional.empty());

		ContributeFundingResponseDto dto = fundingGetUseCase.getParticipatedFunding(fundingId, memberId);

		assertThat(dto.myContribution()).isEqualTo(0);
	}

	@Test
	@DisplayName("getParticipatedFunding 실패: 참여자 아님이면 FORBIDDEN")
	void getParticipatedFunding_Fail_NotParticipated() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView receiver = receiverStub(receiverId);
		given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
		given(memberViewRepository.findById(receiverId)).willReturn(Optional.of(receiver));
		given(participantMemberRepository.existsByFundingIdAndParticipantId(fundingId, memberId)).willReturn(false);

		assertThatThrownBy(() -> fundingGetUseCase.getParticipatedFunding(fundingId, memberId))
				.isInstanceOf(FundingException.class);
	}

	@Test
	@DisplayName("getParticipatedFunding 실패: 펀딩 미존재 FUNDING_NOT_FOUND")
	void getParticipatedFunding_Fail_FundingNotFound() {
		given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingGetUseCase.getParticipatedFunding(fundingId, memberId))
				.isInstanceOf(FundingException.class);
	}

	// ---------- getMyFunding (수령자 단건) ----------

	@Test
	@DisplayName("getMyFunding 성공 IN_PROGRESS: validateReceiver 호출 후 일반 DTO 반환")
	void getMyFunding_Success_InProgress() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		given(fundingRepository.findByIdAndStatus(fundingId, FundingStatus.IN_PROGRESS))
				.willReturn(Optional.of(funding));
		given(funding.isAchieved()).willReturn(false);

		MyFundingResponseDto dto = fundingGetUseCase.getMyFunding(fundingId, FundingStatus.IN_PROGRESS, memberId);

		assertThat(dto.fundingId()).isEqualTo(fundingId);
		assertThat(dto.participants()).isNull();
	}

	@Test
	@DisplayName("getMyFunding 성공 ACHIEVED: 참여자 리스트 포함된 DTO 반환")
	void getMyFunding_Success_Achieved() {
		Funding funding = fundingStub(FundingStatus.ACHIEVED);
		given(fundingRepository.findByIdAndStatus(fundingId, FundingStatus.ACHIEVED))
				.willReturn(Optional.of(funding));
		given(funding.isAchieved()).willReturn(true);

		FundingParticipantMember p1 = mock(FundingParticipantMember.class);
		lenient().when(p1.getParticipantId()).thenReturn(11L);
		lenient().when(p1.getNickName()).thenReturn("참여자1");
		given(participantMemberRepository.findByFundingId(fundingId)).willReturn(List.of(p1));

		MyFundingResponseDto dto = fundingGetUseCase.getMyFunding(fundingId, FundingStatus.ACHIEVED, memberId);

		assertThat(dto.participants()).hasSize(1);
	}

	@Test
	@DisplayName("getMyFunding 실패: 펀딩 미존재 FUNDING_NOT_FOUND")
	void getMyFunding_Fail_NotFound() {
		given(fundingRepository.findByIdAndStatus(fundingId, FundingStatus.IN_PROGRESS))
				.willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingGetUseCase.getMyFunding(fundingId, FundingStatus.IN_PROGRESS, memberId))
				.isInstanceOf(FundingException.class);
	}

	// ---------- getMyFundings (수령자 리스트) ----------

	@Test
	@DisplayName("getMyFundings 성공: 페이지 응답 반환")
	void getMyFundings_Success() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		Page<Funding> page = new PageImpl<>(List.of(funding));
		given(fundingRepository.findAllByReceiverIdAndStatus(any(Long.class), any(FundingStatus.class),
				any(Pageable.class)))
				.willReturn(page);

		PageResponse<MyFundingSummaryDto> result = fundingGetUseCase.getMyFundings(
				0, 10, FundingStatus.IN_PROGRESS, memberId);

		assertThat(result.content()).hasSize(1);
	}

	// ---------- checkFundingExistsByProductId ----------

	@Test
	@DisplayName("checkFundingExistsByProductId: 활성 상태 펀딩 존재 시 true")
	void checkFundingExistsByProductId_True() {
		given(fundingRepository.existsByProductIdAndStatusIn(any(Long.class), any())).willReturn(true);

		boolean result = fundingGetUseCase.checkFundingExistsByProductId(productId);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("checkFundingExistsByProductId: 활성 상태 펀딩 없음 시 false")
	void checkFundingExistsByProductId_False() {
		given(fundingRepository.existsByProductIdAndStatusIn(any(Long.class), any())).willReturn(false);

		boolean result = fundingGetUseCase.checkFundingExistsByProductId(productId);

		assertThat(result).isFalse();
	}

	// ---------- getFriendFundings / getFriendFunding ----------

	@Test
	@DisplayName("getFriendFundings 성공: 친구 관계일 때 친구의 펀딩 리스트 반환")
	void getFriendFundings_Success() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView friend = receiverStub(friendId);
		Page<Funding> page = new PageImpl<>(List.of(funding));
		given(friendshipVerificationPort.areFriends(memberId, friendId)).willReturn(true);
		given(fundingRepository.findAllByReceiverIdAndStatusIn(any(Long.class), any(), any(Pageable.class)))
				.willReturn(page);
		given(memberViewRepository.findById(friendId)).willReturn(Optional.of(friend));

		PageResponse<FundingResponseDto> result = fundingGetUseCase.getFriendFundings(0, 10, memberId, friendId);

		assertThat(result.content()).hasSize(1);
	}

	@Test
	@DisplayName("getFriendFundings 실패: 친구 관계 아님 FORBIDDEN")
	void getFriendFundings_Fail_NotFriend() {
		given(friendshipVerificationPort.areFriends(memberId, friendId)).willReturn(false);

		assertThatThrownBy(() -> fundingGetUseCase.getFriendFundings(0, 10, memberId, friendId))
				.isInstanceOf(FundingException.class);
	}

	@Test
	@DisplayName("getFriendFunding 성공: 친구의 단건 펀딩 조회")
	void getFriendFunding_Success() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView friend = receiverStub(friendId);
		given(friendshipVerificationPort.areFriends(memberId, friendId)).willReturn(true);
		given(fundingRepository.findByIdAndReceiverIdAndStatus(fundingId, friendId, FundingStatus.IN_PROGRESS))
				.willReturn(Optional.of(funding));
		given(memberViewRepository.findById(friendId)).willReturn(Optional.of(friend));

		FundingResponseDto dto = fundingGetUseCase.getFriendFunding(friendId, fundingId, memberId);

		assertThat(dto.fundingId()).isEqualTo(fundingId);
	}

	@Test
	@DisplayName("getFriendFunding 실패: 친구 관계 아님 FORBIDDEN")
	void getFriendFunding_Fail_NotFriend() {
		given(friendshipVerificationPort.areFriends(memberId, friendId)).willReturn(false);

		assertThatThrownBy(() -> fundingGetUseCase.getFriendFunding(friendId, fundingId, memberId))
				.isInstanceOf(FundingException.class);
	}

	@Test
	@DisplayName("getFriendFunding 실패: 친구 펀딩 미존재 FUNDING_NOT_FOUND")
	void getFriendFunding_Fail_NotFound() {
		given(friendshipVerificationPort.areFriends(memberId, friendId)).willReturn(true);
		given(fundingRepository.findByIdAndReceiverIdAndStatus(fundingId, friendId, FundingStatus.IN_PROGRESS))
				.willReturn(Optional.empty());

		assertThatThrownBy(() -> fundingGetUseCase.getFriendFunding(friendId, fundingId, memberId))
				.isInstanceOf(FundingException.class);
	}

	@Test
	@DisplayName("getFriendsFundings 성공: 친구 리스트의 펀딩 반환")
	void getFriendsFundings_Success() {
		Funding funding = fundingStub(FundingStatus.IN_PROGRESS);
		MemberView receiver = receiverStub(receiverId);
		Page<Funding> page = new PageImpl<>(List.of(funding));
		given(friendshipVerificationPort.getFriendIds(memberId)).willReturn(List.of(friendId));
		given(fundingRepository.findAllByReceiverIdInAndStatus(any(), any(FundingStatus.class), any(Pageable.class)))
				.willReturn(page);
		given(memberViewRepository.findAllById(anySet())).willReturn(List.of(receiver));

		PageResponse<FundingResponseDto> result = fundingGetUseCase.getFriendsFundings(0, 10, memberId);

		assertThat(result.content()).hasSize(1);
	}

	@Test
	@DisplayName("getFriendsFundings 성공: 친구 없음 시 빈 페이지 반환")
	void getFriendsFundings_Success_NoFriends() {
		given(friendshipVerificationPort.getFriendIds(memberId)).willReturn(Collections.emptyList());

		PageResponse<FundingResponseDto> result = fundingGetUseCase.getFriendsFundings(0, 10, memberId);

		assertThat(result.content()).isEmpty();
	}
}
