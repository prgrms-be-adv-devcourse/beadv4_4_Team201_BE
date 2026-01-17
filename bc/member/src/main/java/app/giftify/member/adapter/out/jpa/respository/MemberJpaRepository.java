package app.giftify.member.adapter.out.jpa.respository;

import app.giftify.member.adapter.out.jpa.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

    // Auth0 고유 식별자(sub) 통해 엔티티 찾기
    Optional<MemberJpaEntity> findByAuthSub(String authSub);

    // 이메일 통해 엔티티 찾기
    Optional<MemberJpaEntity> findByEmail(String email);
}
