package app.giftify.app;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.domain.FundingMember;
import app.giftify.out.FundingMemberRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FundingMemberSyncUseCase 테스트")
class FundingMemberSyncUseCaseTest {

	@Mock
	private FundingMemberRepository fundingMemberRepository;

	@InjectMocks
	private FundingMemberSyncUseCase fundingMemberSyncUseCase;

	@Test
	@DisplayName("syncMember - FundingMember를 생성하고 저장한다")
	void syncMember_savesNewFundingMember() {
		// given
		Long memberId = 1L;
		String authSub = "google_12345";
		String nickname = "테스트유저";

		when(fundingMemberRepository.save(any(FundingMember.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		// when
		fundingMemberSyncUseCase.syncMember(memberId, authSub, nickname);

		// then
		ArgumentCaptor<FundingMember> captor = ArgumentCaptor.forClass(FundingMember.class);
		verify(fundingMemberRepository, times(1)).save(captor.capture());

		FundingMember savedMember = captor.getValue();
		assertThat(savedMember.getId()).isEqualTo(memberId);
		assertThat(savedMember.getAuthSub()).isEqualTo(authSub);
		assertThat(savedMember.getNickname()).isEqualTo(nickname);
	}

	@Test
	@DisplayName("syncMember - 동일 memberId로 호출 시 업데이트된다 (upsert)")
	void syncMember_updatesExistingMember() {
		// given
		Long memberId = 1L;
		String authSub = "google_12345";
		String originalNickname = "원래닉네임";
		String updatedNickname = "변경된닉네임";

		when(fundingMemberRepository.save(any(FundingMember.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		// when
		fundingMemberSyncUseCase.syncMember(memberId, authSub, originalNickname);
		fundingMemberSyncUseCase.syncMember(memberId, authSub, updatedNickname);

		// then
		ArgumentCaptor<FundingMember> captor = ArgumentCaptor.forClass(FundingMember.class);
		verify(fundingMemberRepository, times(2)).save(captor.capture());

		FundingMember lastSavedMember = captor.getAllValues().get(1);
		assertThat(lastSavedMember.getId()).isEqualTo(memberId);
		assertThat(lastSavedMember.getNickname()).isEqualTo(updatedNickname);
	}

	@Test
	@DisplayName("syncMember - 여러 회원 동기화가 독립적으로 동작한다")
	void syncMember_handlesMultipleMembersIndependently() {
		// given
		Long memberId1 = 1L;
		Long memberId2 = 2L;
		String authSub1 = "google_111";
		String authSub2 = "kakao_222";
		String nickname1 = "유저1";
		String nickname2 = "유저2";

		when(fundingMemberRepository.save(any(FundingMember.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		// when
		fundingMemberSyncUseCase.syncMember(memberId1, authSub1, nickname1);
		fundingMemberSyncUseCase.syncMember(memberId2, authSub2, nickname2);

		// then
		ArgumentCaptor<FundingMember> captor = ArgumentCaptor.forClass(FundingMember.class);
		verify(fundingMemberRepository, times(2)).save(captor.capture());

		FundingMember firstMember = captor.getAllValues().get(0);
		FundingMember secondMember = captor.getAllValues().get(1);

		assertThat(firstMember.getId()).isEqualTo(memberId1);
		assertThat(firstMember.getNickname()).isEqualTo(nickname1);

		assertThat(secondMember.getId()).isEqualTo(memberId2);
		assertThat(secondMember.getNickname()).isEqualTo(nickname2);
	}
}