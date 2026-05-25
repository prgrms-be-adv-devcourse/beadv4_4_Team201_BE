package app.giftify.auth.adapter.inbound.web.dto;

import app.giftify.support.common.security.MemberInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
	@Schema(description = "신규 사용자 여부 (true면 온보딩 필요)", example = "true")
	boolean isNewUser,

	@Schema(description = "Auth0 고유 식별자", example = "auth0|abc123")
	String authSub,

	@Schema(description = "사용자 이메일", example = "user@example.com")
	String email,

	@Schema(description = "사용자 이름 (Auth0에서 제공)", example = "홍길동")
	String name,

	@Schema(description = "회원 정보 (신규 사용자면 null)", nullable = true)
	MemberInfo member
) {
    /**
     * 신규 사용자용 응답 생성
     */
    public static LoginResponse newUser(String authSub, String email, String name) {
        return new LoginResponse(true, authSub, email, name, null);
    }

    /**
     * 기존 회원용 응답 생성
     */
    public static LoginResponse existingMember(MemberInfo member) {
        return new LoginResponse(
                false,
                member.authSub(),
                member.email(),
                member.nickname(),  // MemberInfo에 name이 없어 nickname 사용
                member
        );
    }
}
