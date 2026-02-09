package app.giftify.auth.adapter.inbound.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import app.giftify.auth.adapter.inbound.web.dto.LoginRequest;
import app.giftify.auth.adapter.inbound.web.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Auth V2", description = "인증 관련 API")
public interface AuthV2ApiSpec {

	@Operation(
		summary = "로그인 (SPA SDK용)",
		description = """
			Auth0 SPA SDK에서 발급받은 idToken을 검증하고 회원 정보를 반환합니다.

			**플로우**:
			1. 프론트엔드에서 Auth0 SDK로 로그인
			2. 발급받은 idToken을 이 API로 전송
			3. 신규 사용자면 자동으로 회원 생성 후 `isNewUser: true` 반환
			4. 기존 회원이면 `isNewUser: false`와 회원 정보 반환

			**신규 사용자 처리**:
			- 닉네임은 `User_XXXX` 형식으로 자동 생성됩니다
			- 추가 정보(생년월일, 주소 등)는 온보딩 화면에서 `PATCH /api/v2/members/me`로 입력받으세요
			"""
	)
	@ApiResponse(
		responseCode = "200",
		description = "로그인 성공",
		content = @Content(
			schema = @Schema(implementation = LoginResponse.class),
			examples = {
				@ExampleObject(
					name = "신규 사용자",
					summary = "처음 로그인하는 사용자",
					value = """
						{
						  "isNewUser": true,
						  "authSub": "auth0|abc123",
						  "email": "user@example.com",
						  "name": "홍길동",
						  "member": null
						}
						"""
				),
				@ExampleObject(
					name = "기존 회원",
					summary = "이미 가입된 회원",
					value = """
						{
						  "isNewUser": false,
						  "authSub": "auth0|abc123",
						  "email": "user@example.com",
						  "name": "홍길동",
						  "member": {
						    "memberId": 1,
						    "authSub": "auth0|abc123",
						    "email": "user@example.com",
						    "nickname": "User_4829"
						  }
						}
						"""
				)
			}
		)
	)
	@ApiResponse(
		responseCode = "400",
		description = "잘못된 요청 (idToken 누락)",
		content = @Content
	)
	@ApiResponse(
		responseCode = "401",
		description = "유효하지 않은 idToken",
		content = @Content
	)
	ResponseEntity<LoginResponse> login(
		@RequestBody @Valid LoginRequest request
	);
}
