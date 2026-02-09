package app.giftify.member.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.member.adapter.in.web.dto.MemberResponse;
import app.giftify.member.adapter.in.web.dto.MemberUpdateRequest;
import app.giftify.member.adapter.in.web.dto.NicknameCheckResponse;
import app.giftify.member.adapter.in.web.dto.RegistrationStatusResponse;
import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.domain.exception.InvalidNicknameException;
import app.giftify.member.domain.exception.MemberNotFoundException;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.security.common.CurrentAuthSub;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Member API v2.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/members")
@RequiredArgsConstructor
@Validated
public class MemberV2Controller implements MemberV2Api {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final WithdrawMemberUseCase withdrawMemberUseCase;

    // ========== 가입된 회원 전용 (memberId 사용) ==========

    /**
     * 내 정보 조회.
     *
     * @param memberId DB의 회원 PK (MemberPrincipalFilter에서 주입)
     */
    @Override
    @GetMapping("/me")
    public ResponseEntity<RsData<MemberResponse>> getMe(
            @CurrentMemberId Long memberId
    ) {
        if (memberId == null) {
            // 미가입 사용자가 호출한 경우
            throw new MemberNotFoundException("[MemberV2Controller] 미가입 사용자입니다.");
        }

        Member member = getMemberUseCase.getMemberById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        return ResponseEntity.ok(RsData.success(MemberResponse.from(member)));
    }

    /**
     * 회원 정보 수정.
     *
     * @param memberId DB의 회원 PK (MemberPrincipalFilter에서 주입)
     */
    @Override
    @PatchMapping("/me")
    public ResponseEntity<RsData<MemberResponse>> updateMe(
            @CurrentMemberId Long memberId,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        if (memberId == null) {
            throw new MemberNotFoundException("[MemberV2Controller] 미가입 사용자입니다.");
        }

        Member member = getMemberUseCase.getMemberById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            return ResponseEntity.status(403)
                    .body(RsData.failWithType("탈퇴한 회원은 정보를 수정할 수 없습니다.", "MEMBER_WITHDRAWN"));
        }

        UpdateMemberUseCase.UpdateCommand command = new UpdateMemberUseCase.UpdateCommand(
                member.getAuthSub(),  // authSub는 member에서 추출
                request.password(),
                request.nickname(),
                request.address(),
                request.phoneNum(),
                request.name()
        );

        Member updatedMember = updateMemberUseCase.updateMember(command);
        return ResponseEntity.ok(RsData.success(MemberResponse.from(updatedMember)));
    }

    /**
     * 회원 탈퇴.
     *
     * @param memberId DB의 회원 PK (MemberPrincipalFilter에서 주입)
     */
    @Override
    @DeleteMapping("/me")
    public ResponseEntity<RsData<Void>> withdraw(
            @CurrentMemberId Long memberId
    ) {
        if (memberId == null) {
            throw new MemberNotFoundException("[MemberV2Controller] 미가입 사용자입니다.");
        }

        Member member = getMemberUseCase.getMemberById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        withdrawMemberUseCase.withdrawMember(member.getAuthSub());

        return ResponseEntity.noContent().build();
    }

    // ========== 미가입 사용자도 호출 가능 (authSub 사용) ==========

    /**
     * 회원가입 (추가 정보 입력).
     *
     * @param authSub Auth0 식별자 (미가입 사용자도 값 있음)
     */
    @Override
    @PostMapping("/signup")
    public ResponseEntity<RsData<MemberResponse>> signup(
            @CurrentAuthSub String authSub,
            @RequestBody @Valid SignupRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401)
                    .body(RsData.failWithType("인증 정보가 누락되었습니다.", "AUTH_REQUIRED"));
        }

        // 이미 가입된 회원인지 확인
        if (getMemberUseCase.getMemberByAuthSub(authSub).isPresent()) {
            return ResponseEntity.status(409)
                    .body(RsData.failWithType("이미 가입된 회원입니다.", "MEMBER_ALREADY_EXISTS"));
        }

        Member member = registerMemberUseCase.signup(authSub, request);
        return ResponseEntity.ok(RsData.success(MemberResponse.from(member)));
    }

    /**
     * 가입 여부 확인.
     *
     * @param authSub Auth0 식별자 (미가입 사용자도 값 있음)
     */
    @Override
    @GetMapping("/check-registration")
    public ResponseEntity<RsData<RegistrationStatusResponse>> checkRegistration(
            @CurrentAuthSub String authSub
    ) {
        log.debug("[MemberV2Controller] checkRegistration called with authSub: {}", authSub);

        if (authSub == null) {
            return ResponseEntity.status(401)
                    .body(RsData.failWithType("인증 정보(JWT)가 누락되었습니다.", "AUTH_REQUIRED"));
        }

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(member -> {
                    log.debug("[MemberV2Controller] Member found for authSub: {}", authSub);
                    return ResponseEntity.ok(
                            RsData.success(RegistrationStatusResponse.registered(MemberResponse.from(member)))
                    );
                })
                .orElseGet(() -> {
                    log.debug("[MemberV2Controller] Member NOT found for authSub: {}", authSub);
                    return ResponseEntity.ok(
                            RsData.success(RegistrationStatusResponse.notRegistered())
                    );
                });
    }

    // ========== 인증 불필요 ==========

    /**
     * 닉네임 중복 확인.
     */
    @Override
    @GetMapping("/check/nickname")
    public ResponseEntity<RsData<NicknameCheckResponse>> checkNickname(
            @RequestParam(name = "nickname") @NotBlank String nickname
    ) {
        if (nickname.isBlank()) {
            throw new InvalidNicknameException();
        }

        boolean duplicated = getMemberUseCase.isNicknameDuplicated(nickname);
        return ResponseEntity.ok(
                RsData.success(NicknameCheckResponse.of(nickname, duplicated))
        );
    }
}
