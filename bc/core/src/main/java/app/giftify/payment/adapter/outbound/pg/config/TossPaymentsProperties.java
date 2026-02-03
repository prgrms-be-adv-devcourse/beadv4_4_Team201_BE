package app.giftify.payment.adapter.outbound.pg.config;

import lombok.Data;

@Data
public class TossPaymentsProperties {

	private Api api = new Api();
	private String secretKey;

	@Data
	public static class Api {
		private String baseUrl = "https://api.tosspayments.com";
	}
}
