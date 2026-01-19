package app.giftify.shared.api.response;

public record ErrorResponse( // NOTE :: Shared 모듈에 공통 에러 응답 DTO 추가됨, 주의 요망
	String code,
	String message
) {
}
