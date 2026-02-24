package app.giftify.auth.adapter.outbound.client;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface Auth0ApiClient {

	@PostExchange(url = "/oauth/revoke", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	void revokeToken(@RequestBody MultiValueMap<String, String> params);
}
