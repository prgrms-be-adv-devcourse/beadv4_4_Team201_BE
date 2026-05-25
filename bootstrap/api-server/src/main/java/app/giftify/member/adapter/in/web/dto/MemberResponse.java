package app.giftify.member.adapter.in.web.dto;

import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.support.common.security.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "회원 정보 응답")
public record MemberResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "닉네임", example = "행복한고양이1234")
        String nickname,

        @Schema(description = "생년월일", example = "1990-01-15")
        LocalDate birthday,

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
        String address,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNum,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "회원 상태", example = "ACTIVE")
        MemberStatus status,

        @Schema(description = "회원 권한", example = "BUYER")
        MemberRole role
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getBirthday(),
                member.getAddress(),
                member.getPhoneNum(),
                member.getName(),
                member.getStatus(),
                member.getRole()
        );
    }
}
