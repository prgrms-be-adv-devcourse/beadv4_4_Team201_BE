package app.giftify.payment.adapter.outbound.pg.config;

import java.time.Duration;

import lombok.Data;

@Data
public class TossPaymentsProperties {

	private Api api = new Api();
	private String secretKey;
	private Timeout timeout = new Timeout();

	@Data
	public static class Api {
		private String baseUrl = "https://api.tosspayments.com";
	}

	@Data
	public static class Timeout {
		private Duration connect = Duration.ofSeconds(5);
		private Duration read = Duration.ofSeconds(30);
	}
}
