package app.giftify.payment.adapter.in.web.payment.dto;

public record PgCancelWebhookRequest(
	String paymentKey,
	String reason,
	String canceledAt
) {
}
