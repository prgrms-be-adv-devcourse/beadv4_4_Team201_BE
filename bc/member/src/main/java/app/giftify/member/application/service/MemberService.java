package app.giftify.member.application.service;

import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.core.domain.exception.DuplicateMemberException;
import app.giftify.member.core.domain.exception.MemberNotFoundException;
import app.giftify.member.core.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements GetMemberUseCase, RegisterMemberUseCase, UpdateMemberUseCase, WithdrawMemberUseCase {

    private final MemberRepositoryPort memberRepositoryPort;

    @Override
    public Optional<Member> getMemberByAuthSub(String authSub) {
        return memberRepositoryPort.findByAuthSub(authSub);
    }

    @Override
    public Optional<Member> getMemberById(Long id) {
        return memberRepositoryPort.findById(id);
    }

    @Override
    @Transactional
    public Member registerMember(RegisterCommand command) {
        // [중복 가입 방지] 이미 가입된 회원인지 한 번 더 검증
        memberRepositoryPort.findByAuthSub(command.authSub())
                .ifPresent(m -> {
                    throw new DuplicateMemberException(command.email());
                });

        Member newMember = Member.builder()
                .email(command.email())
                .authSub(command.authSub())
                .nickname(command.nickname())
                .birthday(command.birthday())
                .address(command.address())
                .phoneNum(command.phoneNum())
                .name(command.name())
                .build();

        return memberRepositoryPort.save(newMember);
    }

    @Override
    @Transactional
    public Member updateMember(UpdateCommand command) {
        Member member = memberRepositoryPort.findByAuthSub(command.authSub())
                .orElseThrow(() -> new MemberNotFoundException(command.authSub()));

        member.updateInfo(command.nickname(), command.password(), command.address(), command.phoneNum(), command.name());

        return memberRepositoryPort.save(member);
    }

    @Override
    @Transactional
    public void withdrawMember(String authSub) {
        Member member = memberRepositoryPort.findByAuthSub(authSub)
                .orElseThrow(() -> new MemberNotFoundException(authSub));

        member.withdraw();

        memberRepositoryPort.save(member);
    }
}
