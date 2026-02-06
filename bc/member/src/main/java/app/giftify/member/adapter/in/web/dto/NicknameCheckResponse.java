package app.giftify.member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 닉네임 중복 확인 응답 DTO.
 *
 * @param available 사용 가능 여부 (true: 사용 가능, false: 중복)
 * @param nickname 확인한 닉네임
 */
@Schema(description = "닉네임 중복 확인 응답")
public record NicknameCheckResponse(
        @Schema(description = "사용 가능 여부", example = "true")
        boolean available,

        @Schema(description = "확인한 닉네임", example = "홍길동")
        String nickname
) {
    public static NicknameCheckResponse of(String nickname, boolean isDuplicated) {
        return new NicknameCheckResponse(!isDuplicated, nickname);
    }
}
