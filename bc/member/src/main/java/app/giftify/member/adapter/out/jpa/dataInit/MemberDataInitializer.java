package app.giftify.member.adapter.out.jpa.dataInit;

import app.giftify.member.adapter.out.jpa.entity.MemberJpaEntity;
import app.giftify.member.adapter.out.jpa.repository.MemberJpaRepository;
import app.giftify.member.core.domain.MemberRole;
import app.giftify.member.core.domain.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MemberDataInitializer implements ApplicationRunner {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (memberJpaRepository.count() > 0) {
            return;
        }

        MemberJpaEntity member = MemberJpaEntity.builder()
                .email("dev4.team201@gmail.com")
                .password("dev1234!!")
                .nickname("dev4.team201")
                .birthday(LocalDate.of(1970, 11, 8))
                .role(MemberRole.BUYER)
                .address("서울시 송파구")
                .phoneNum("010-1234-5678")
                .name("Team201 DEV4")
                .status(MemberStatus.ACTIVE)
                .authSub("google-oauth2|104844495450678108304")
                .build();

        memberJpaRepository.save(member);
    }
}