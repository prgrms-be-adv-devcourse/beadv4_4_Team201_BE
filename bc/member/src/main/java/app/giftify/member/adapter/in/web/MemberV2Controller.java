package app.giftify.member.adapter.in.web;

import app.giftify.member.adapter.in.web.dto.MemberResponse;
import app.giftify.member.adapter.in.web.dto.MemberUpdateRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.domain.exception.MemberNotFoundException;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.security.common.context.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Member API v2.
 * RESTful 경로와 DTO 응답을 사용합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/members")
@RequiredArgsConstructor
@Validated
public class MemberV2Controller implements MemberV2Api {

    private final GetMemberUseCase getMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;

    /**
     * 내 정보 조회.
     */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(
            @AuthenticatedMember String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(member -> ResponseEntity.ok(MemberResponse.from(member)))
                .orElseThrow(() -> new MemberNotFoundException(authSub));
    }

    /**
     * 회원 정보 수정.
     */
    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(
            @AuthenticatedMember String authSub,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Member> memberOpt = getMemberUseCase.getMemberByAuthSub(authSub);
        if (memberOpt.isPresent() && memberOpt.get().getStatus() != MemberStatus.ACTIVE) {
            return ResponseEntity.status(403).build();
        }

        UpdateMemberUseCase.UpdateCommand command = new UpdateMemberUseCase.UpdateCommand(
                authSub,
                request.password(),
                request.nickname(),
                request.address(),
                request.phoneNum(),
                request.name()
        );

        Member updatedMember = updateMemberUseCase.updateMember(command);
        return ResponseEntity.ok(MemberResponse.from(updatedMember));
    }
}
