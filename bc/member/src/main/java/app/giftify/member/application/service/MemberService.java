package app.giftify.member.application.service;

import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.domain.exception.DuplicateMemberException;
import app.giftify.member.domain.exception.MemberNotFoundException;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.NicknameGenerator;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService
        implements GetMemberUseCase, RegisterMemberUseCase, UpdateMemberUseCase, WithdrawMemberUseCase {

    private final MemberRepositoryPort memberRepositoryPort;
    private final EventPublisher eventPublisher;
    private final NicknameGenerator nicknameGenerator;

    @Override
    public Optional<Member> getMemberByAuthSub(String authSub) {
        return memberRepositoryPort.findByAuthSub(authSub);
    }

    @Override
    public Optional<Member> getMemberById(Long id) {
        return memberRepositoryPort.findById(id);
    }

    @Override
    public Member registerMember(RegisterCommand command) {
        // [중복 가입 방지] 이미 가입된 회원인지 한 번 더 검증
        memberRepositoryPort.findByAuthSub(command.authSub())
                .ifPresent(m -> {
                    throw new DuplicateMemberException(command.authSub());
                });

        // 닉네임이 없으면 자동 생성
        String nickname = command.nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = nicknameGenerator.generate();
            log.info("[Member] 닉네임 자동 생성: {}", nickname);
        }

        Member member = Member.builder()
                .authSub(command.authSub())
                .email(command.email())
                .nickname(nickname)
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
        Member member = memberRepositoryPort.findByAuthSub(authSub)
                .orElseThrow(() -> new MemberNotFoundException(authSub));

        member.updateProfile(request.birthday(), request.address(), request.phoneNum());

        Member updatedMember = memberRepositoryPort.save(member);

        log.info("[Member] 프로필 정보 업데이트 완료: memberId={}", updatedMember.getId());

        return updatedMember;
    }

    @Override
    @Transactional
    public Member updateMember(UpdateCommand command) {
        Member member = memberRepositoryPort.findByAuthSub(command.authSub())
                .orElseThrow(() -> new MemberNotFoundException(command.authSub()));

        member.validateActiveStatus();

        member.updateInfo(command.nickname(), command.password(), command.address(), command.phoneNum(),
                command.name());

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

        member.validateActiveStatus();

        member.withdraw();

        memberRepositoryPort.save(member);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberRepositoryPort.findByEmail(email).isPresent();
    }

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepositoryPort.findByNickname(nickname).isPresent();
    }
}
