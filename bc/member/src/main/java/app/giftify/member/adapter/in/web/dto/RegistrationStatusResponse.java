package app.giftify.member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원 가입 여부 확인 응답 DTO.
 *
 * @param registered 가입 여부 (true: 가입됨, false: 미가입)
 * @param member 가입된 경우 회원 정보, 미가입인 경우 null
 */
@Schema(description = "회원 가입 여부 확인 응답")
public record RegistrationStatusResponse(
        @Schema(description = "가입 여부", example = "true")
        boolean registered,

        @Schema(description = "회원 정보 (가입된 경우에만)")
        MemberResponse member
) {
    public static RegistrationStatusResponse registered(MemberResponse member) {
        return new RegistrationStatusResponse(true, member);
    }

    public static RegistrationStatusResponse notRegistered() {
        return new RegistrationStatusResponse(false, null);
    }
}
