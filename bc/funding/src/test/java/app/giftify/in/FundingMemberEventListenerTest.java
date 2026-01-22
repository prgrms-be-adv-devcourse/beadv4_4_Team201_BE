package app.giftify.in;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.app.FundingMemberSyncUseCase;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;

@ExtendWith(MockitoExtension.class)
class FundingMemberEventListenerTest {

	@Mock
	private FundingMemberSyncUseCase fundingMemberSyncUseCase;

	@InjectMocks
	private FundingMemberEventListener fundingMemberEventListener;

	@Test
	@DisplayName("MemberSignedEvent 수신 시 syncMember 호출")
	void handle_memberSignedEvent_callsSyncMember() {
		// given
		Long memberId = 1L;
		String authSub = "auth0|123456789";
		String nickname = "테스트유저";
		MemberSignedEvent event = new MemberSignedEvent(memberId, authSub, nickname);

		// when
		fundingMemberEventListener.handle(event);

		// then
		verify(fundingMemberSyncUseCase, times(1)).syncMember(memberId, authSub, nickname);
	}

	@Test
	@DisplayName("MemberUpdatedEvent 수신 시 syncMember 호출")
	void handle_memberUpdatedEvent_callsSyncMember() {
		// given
		Long memberId = 2L;
		String authSub = "auth0|123456789";
		String nickname = "수정된닉네임";
		MemberUpdatedEvent event = new MemberUpdatedEvent(memberId, authSub, nickname);

		// when
		fundingMemberEventListener.handle(event);

		// then
		verify(fundingMemberSyncUseCase, times(1)).syncMember(memberId, authSub, nickname);
	}

	@Test
	@DisplayName("MemberSignedEvent - 이벤트 데이터가 정확히 전달됨")
	void handle_memberSignedEvent_passesCorrectData() {
		// given
		Long memberId = 100L;
		String authSub = "auth0|123456789";
		String nickname = "홍길동";
		MemberSignedEvent event = new MemberSignedEvent(memberId, authSub, nickname);

		// when
		fundingMemberEventListener.handle(event);

		// then
		verify(fundingMemberSyncUseCase).syncMember(
			eq(100L),
			eq("auth0|123456789"),
			eq("홍길동")
		);
	}

	@Test
	@DisplayName("MemberUpdatedEvent - 이벤트 데이터가 정확히 전달됨")
	void handle_memberUpdatedEvent_passesCorrectData() {
		// given
		Long memberId = 200L;
		String authSub = "auth0|987654321";
		String nickname = "변경된닉네임";
		MemberUpdatedEvent event = new MemberUpdatedEvent(memberId, authSub, nickname);

		// when
		fundingMemberEventListener.handle(event);

		// then
		verify(fundingMemberSyncUseCase).syncMember(
			eq(200L),
			eq("auth0|987654321"),
			eq("변경된닉네임")
		);
	}

	@Test
	@DisplayName("회원가입 후 정보 수정 시 같은 memberId로 변경된 데이터가 전달됨")
	void handle_signedThenUpdated_syncMemberCalledWithSameIdAndUpdatedData() {
		// given
		Long memberId = 1L;
		String originalAuthSub = "auth0|123456789";
		String originalNickname = "원래닉네임";
		String updatedAuthSub = "auth0|123456789";
		String updatedNickname = "변경된닉네임";

		MemberSignedEvent signedEvent = new MemberSignedEvent(memberId, originalAuthSub, originalNickname);
		MemberUpdatedEvent updatedEvent = new MemberUpdatedEvent(memberId, updatedAuthSub, updatedNickname);

		// when
		fundingMemberEventListener.handle(signedEvent);
		fundingMemberEventListener.handle(updatedEvent);

		// then
		InOrder inOrder = inOrder(fundingMemberSyncUseCase);
		inOrder.verify(fundingMemberSyncUseCase).syncMember(memberId, originalAuthSub, originalNickname);
		inOrder.verify(fundingMemberSyncUseCase).syncMember(memberId, updatedAuthSub, updatedNickname);
		verify(fundingMemberSyncUseCase, times(2)).syncMember(eq(memberId), anyString(), anyString());
	}
}