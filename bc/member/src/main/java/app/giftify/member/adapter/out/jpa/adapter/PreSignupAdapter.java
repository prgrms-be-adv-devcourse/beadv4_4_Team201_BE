package app.giftify.member.adapter.out.jpa.adapter;

import app.giftify.member.adapter.out.jpa.entity.PreSignup;
import app.giftify.member.adapter.out.jpa.repository.PreSignupRepository;
import app.giftify.member.application.port.out.PreSignupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PreSignupAdapter implements PreSignupPort {

    private final PreSignupRepository preSignupRepository;

    @Override
    public PreSignup save(PreSignup preSignup) {
        return preSignupRepository.save(preSignup);
    }

    @Override
    public Optional<PreSignup> findByAuthSub(String authSub) {
        return preSignupRepository.findByAuthSub(authSub);
    }

    @Override
    public void deleteByAuthSub(String authSub) {
        preSignupRepository.deleteByAuthSub(authSub);
    }
}