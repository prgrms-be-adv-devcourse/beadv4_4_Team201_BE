package app.giftify.replica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberReplicaRepository extends JpaRepository<MemberReplica, Long> {
    Optional<MemberReplica> findById(Long id);
}
