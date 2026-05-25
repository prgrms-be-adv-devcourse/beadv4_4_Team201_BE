package app.giftify.member.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.auth.application.TokenBlacklistService;
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
import app.giftify.support.common.event.EventPublisher;
import app.giftify.member.domain.event.MemberSignedEvent;
import app.giftify.member.domain.event.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService
        implements GetMemberUseCase, RegisterMemberUseCase, UpdateMemberUseCase, WithdrawMemberUseCase {
	private static final Logger log = LoggerFactory.getLogger(MemberService.class);


    private final MemberRepositoryPort memberRepositoryPort;
    private final EventPublisher eventPublisher;
    private final NicknameGenerator nicknameGenerator;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMemberByAuthSub(String authSub) {
        return memberRepositoryPort.findByAuthSub(authSub);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMemberById(Long id) {
        return memberRepositoryPort.findById(id);
    }

    @Override
    public Member registerMember(RegisterCommand command) {
        Optional<Member> existingMember = memberRepositoryPort.findByAuthSub(command.authSub());
        if (existingMember.isPresent()) {
            Member member = existingMember.get();
            if (member.isWithdrawn()) {
                member.reactivate();
                Member reactivated = memberRepositoryPort.save(member);
                log.info("[MemberService] 탈퇴 회원 재활성화: memberId={}", reactivated.getId());
                return reactivated;
            }
            throw new DuplicateMemberException(command.authSub());
        }

        // 닉네임이 없으면 자동 생성
        String nickname = command.nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = nicknameGenerator.generate();
            log.info("[MemberService] 닉네임 자동 생성: {}", nickname);
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
    public Member signup(String authSub, SignupRequest request) {
        Member member = memberRepositoryPort.findByAuthSub(authSub)
                .orElseThrow(() -> new MemberNotFoundException(authSub));

        member.updateProfile(request.birthday(), request.address(), request.phoneNum());

        if (request.nickname() != null && !request.nickname().isBlank()) {
            member.updateInfo(request.nickname(), null, null, null);
        }

        Member updatedMember = memberRepositoryPort.save(member);

        log.info("[MemberService] 프로필 정보 업데이트 완료: memberId={}", updatedMember.getId());

        return updatedMember;
    }

    @Override
    public Member updateMember(UpdateCommand command) {
        Member member = memberRepositoryPort.findByAuthSub(command.authSub())
                .orElseThrow(() -> new MemberNotFoundException(command.authSub()));

        member.validateActiveStatus();

        member.updateInfo(command.nickname(), command.address(), command.phoneNum(),
                command.name());

        Member updatedMember = memberRepositoryPort.save(member);

        eventPublisher.publish(
                new MemberUpdatedEvent(
                        updatedMember.getId(),
                        updatedMember.getAuthSub(),
                        updatedMember.getNickname(),
                        updatedMember.getRole()
                )
        );

        log.info("[MemberService] 회원 정보 업데이트 완료: memberId={}", updatedMember.getId());

        return updatedMember;
    }

    @Override
    public void withdrawMember(String authSub) {
        Member member = memberRepositoryPort.findByAuthSub(authSub)
                .orElseThrow(() -> new MemberNotFoundException(authSub));

        member.validateActiveStatus();

        member.withdraw();

        memberRepositoryPort.save(member);

        // 토큰 무효화 - 탈퇴 후 기존 토큰으로 API 호출 방지
        tokenBlacklistService.revokeAllUserTokens(authSub);

        log.info("[MemberService] 회원 탈퇴 및 토큰 무효화: authSub={}", authSub);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return memberRepositoryPort.findByEmail(email).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepositoryPort.findByNickname(nickname).isPresent();
    }
}
