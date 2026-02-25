package app.giftify.auth.adapter.inbound.web.dto;

public record LogoutRequest(
	String refreshToken
) {}
