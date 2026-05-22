package app.giftify.auth.adapter.outbound.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class Auth0RevokeClient {
	private static final Logger log = LoggerFactory.getLogger(Auth0RevokeClient.class);


	private final Auth0ApiClient auth0ApiClient;
	private final String clientId;
	private final String clientSecret;

	public Auth0RevokeClient(
		Auth0ApiClient auth0ApiClient,
		@Value("${spring.security.oauth2.client.registration.auth0.client-id}") String clientId,
		@Value("${spring.security.oauth2.client.registration.auth0.client-secret}") String clientSecret
	) {
		this.auth0ApiClient = auth0ApiClient;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public void revokeRefreshToken(String refreshToken) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", clientId);
		body.add("client_secret", clientSecret);
		body.add("token", refreshToken);

		auth0ApiClient.revokeToken(body);
		log.info("[Auth0] Refresh token revoked successfully");
	}
}
