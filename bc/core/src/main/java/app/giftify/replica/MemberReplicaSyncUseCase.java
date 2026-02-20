package app.giftify.replica;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberReplicaSyncUseCase {
    private final MemberReplicaRepository memberReplicaRepository;

    public void syncMember(Long id, String nickname) {
        memberReplicaRepository.findById(id)
                .ifPresentOrElse(
                        existing -> existing.update(nickname),
                        () -> memberReplicaRepository.save(new MemberReplica(id, nickname))
                );
    }
}
