package app.giftify.member.adapter.in.web.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// 회원가입 정보 수신
// 프론트엔드로부터 회원가입 시 추가로 입력받는 정보를 담는 객체
// 이메일이나 식별자(sub)는 보안을 위해 JWT에서 직접 추출(클라이언트 임의 조작 X)하므로 포함 X
public record SignupRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthday,

        @NotBlank(message = "배송지 주소는 필수입니다.")
        String address,

        @NotNull(message = "전화번호는 필수입니다.")
        String phoneNum,

        @NotBlank(message = "실명은 필수입니다.")
        String name
) {
}
