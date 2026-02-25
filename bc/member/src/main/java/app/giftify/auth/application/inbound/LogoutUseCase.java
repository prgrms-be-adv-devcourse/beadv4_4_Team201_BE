package app.giftify.auth.application.inbound;

public interface LogoutUseCase {

	void logout(LogoutCommand command);

	record LogoutCommand(
		String accessToken,
		String refreshToken
	) {}
}
