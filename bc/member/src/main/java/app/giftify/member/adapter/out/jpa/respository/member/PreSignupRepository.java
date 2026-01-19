package app.giftify.member.adapter.out.jpa.respository.member;

import app.giftify.member.adapter.out.jpa.entity.member.PreSignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreSignupRepository extends JpaRepository<PreSignup, String> {
    Optional<PreSignup> findByAuthSub(String authSub);

    void deleteByEmail(String email);

    void deleteByAuthSub(String authSub);
}
