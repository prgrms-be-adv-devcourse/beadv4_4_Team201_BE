package app.giftify.replica.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSyncUseCase {
    private final MemberRepository memberRepository;

    @Transactional
    public void syncMember(Long memberId, String nickname) {
        // 기존 회원 존재 시 업데이트, 없으면 생성
        memberRepository.findById(memberId)
                .ifPresentOrElse(
                        existing -> existing.update(nickname),
                        () -> memberRepository.save(new Member(memberId, nickname))
                );
    }
}
