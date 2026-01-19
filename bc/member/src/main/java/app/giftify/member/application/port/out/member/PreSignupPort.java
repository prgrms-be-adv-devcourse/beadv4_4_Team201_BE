package app.giftify.member.application.port.out.member;

import app.giftify.member.adapter.out.jpa.entity.member.PreSignup;

import java.util.Optional;

public interface PreSignupPort {
    PreSignup save(PreSignup preSignup);

    Optional<PreSignup> findByAuthSub(String authSub);

    void deleteByAuthSub(String authSub);
}
