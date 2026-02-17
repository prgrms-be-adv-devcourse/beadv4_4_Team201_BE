package app.giftify.replica;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberReplicaSyncUseCase {
    private final MemberReplicaRepository memberReplicaRepository;

    public void syncMember(Long memberId, String authSub, String nickname) {
        memberReplicaRepository.findById(memberId)
                .ifPresentOrElse(
                        existing -> existing.update(authSub, nickname),
                        () -> memberReplicaRepository.save(new MemberReplica(memberId, authSub, nickname))
                );
    }
}
