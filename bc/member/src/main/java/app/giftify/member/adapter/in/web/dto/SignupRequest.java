package app.giftify.member.adapter.in.web.dto;

import java.time.LocalDate;

/**
 * 회원가입 시 추가 정보를 수신하는 DTO.
 * 모든 필드가 Optional이므로 빈 body로도 가입 가능합니다.
 * 닉네임은 미입력 시 서버에서 자동 생성됩니다.
 */
public record SignupRequest(
	LocalDate birthday,
	String address,
	String phoneNum
) {
}
