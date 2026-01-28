package app.giftify.app;

import org.springframework.stereotype.Service;

import app.giftify.domain.FundingMember;
import app.giftify.out.FundingMemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FundingMemberSyncUseCase {
	private final FundingMemberRepository fundingMemberRepository;

	public void syncMember(Long memberId, String authSub, String nickname) {
		// 기존 회원 존재 시 업데이트, 없으면 생성
		fundingMemberRepository.findById(memberId)
			.ifPresentOrElse(
				existing -> existing.update(authSub, nickname),
				() -> fundingMemberRepository.save(new FundingMember(memberId, authSub, nickname))
			);
	}
}
