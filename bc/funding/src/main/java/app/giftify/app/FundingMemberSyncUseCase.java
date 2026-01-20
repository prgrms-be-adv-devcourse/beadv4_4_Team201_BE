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
		FundingMember seller = new FundingMember(memberId, authSub, nickname);
		fundingMemberRepository.save(seller);
	}
}
