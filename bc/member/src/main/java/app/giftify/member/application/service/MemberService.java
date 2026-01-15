package app.giftify.member.application.service;

import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.core.domain.member.Member;
import app.giftify.member.core.exception.DuplicateMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements GetMemberUseCase, RegisterMemberUseCase {

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
}
