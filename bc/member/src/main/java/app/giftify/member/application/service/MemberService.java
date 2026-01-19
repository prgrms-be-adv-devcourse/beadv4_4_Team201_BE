package app.giftify.member.application.service;

import app.giftify.member.adapter.in.web.requestDto.SignupRequest;
import app.giftify.member.adapter.out.jpa.entity.PreSignup;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.application.port.out.PreSignupPort;
import app.giftify.member.core.domain.exception.DuplicateMemberException;
import app.giftify.member.core.domain.exception.MemberNotFoundException;
import app.giftify.member.core.domain.member.Member;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements GetMemberUseCase, RegisterMemberUseCase, UpdateMemberUseCase, WithdrawMemberUseCase {

    private final PreSignupPort preSignupPort;
    private final MemberRepositoryPort memberRepositoryPort;
    private final EventPublisher eventPublisher;

    @Override
    public Optional<Member> getMemberByAuthSub(String authSub) {
        return memberRepositoryPort.findByAuthSub(authSub);
    }

    @Override
    public Optional<Member> getMemberById(Long id) {
        return memberRepositoryPort.findById(id);
    }

    @Override
    public Member joinedMember(RegisterCommand command) {
        // [중복 가입 방지] 이미 가입된 회원인지 한 번 더 검증
        memberRepositoryPort.findByAuthSub(command.authSub())
                .ifPresent(m -> {
                    throw new DuplicateMemberException(command.authSub());
                });

        Member member = Member.builder()
                .authSub(command.authSub())
                .email(command.email())
                .nickname(command.nickname())
                .birthday(command.birthday())
                .address(command.address())
                .phoneNum(command.phoneNum())
                .name(command.name())
                .build();

        Member savedMember = memberRepositoryPort.save(member);

        eventPublisher.publish(
                new MemberSignedEvent(
                        savedMember.getId(),
                        savedMember.getAuthSub(),
                        savedMember.getNickname()

                )
        );

        return savedMember;
    }

    @Override
    @Transactional
    public Member signup(String authSub, SignupRequest request) {
        PreSignup preSignup = preSignupPort.findByAuthSub(authSub)
                .orElseThrow(() -> new RuntimeException("임시 회원 정보가 없습니다."));

        RegisterCommand command = new RegisterCommand(
                preSignup.getEmail(),
                preSignup.getNickname(),
                request.birthday(),
                request.address(),
                request.phoneNum(),
                preSignup.getName(),
                authSub
        );

        Member member = joinedMember(command);

        preSignupPort.deleteByAuthSub(authSub);

        return member;
    }

    @Override
    @Transactional
    public Member updateMember(UpdateCommand command) {
        Member member = memberRepositoryPort.findByAuthSub(command.authSub())
                .orElseThrow(() -> new MemberNotFoundException(command.authSub()));

        member.validActive();

        member.updateInfo(command.nickname(), command.password(), command.address(), command.phoneNum(), command.name());

        Member updatedMember = memberRepositoryPort.save(member);

        eventPublisher.publish(
                new MemberUpdatedEvent(
                        updatedMember.getId(),
                        updatedMember.getAuthSub(),
                        updatedMember.getNickname()
                )
        );

        return updatedMember;
    }

    @Override
    @Transactional
    public void withdrawMember(String authSub) {
        Member member = memberRepositoryPort.findByAuthSub(authSub)
                .orElseThrow(() -> new MemberNotFoundException(authSub));

        member.validActive();

        member.withdraw();

        memberRepositoryPort.save(member);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberRepositoryPort.findByEmail(email).isPresent();
    }
}
