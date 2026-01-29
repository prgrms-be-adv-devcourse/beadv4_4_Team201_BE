package app.giftify.funding.app;

import org.springframework.stereotype.Service;

import app.giftify.funding.domain.FundingMember;
import app.giftify.funding.out.FundingMemberRepository;
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
