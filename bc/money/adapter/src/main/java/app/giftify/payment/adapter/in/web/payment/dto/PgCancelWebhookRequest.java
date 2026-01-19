package app.giftify.payment.adapter.in.web.payment.dto;

public record PgCancelWebhookRequest(
	String pgTransactionId,
	String reason,
	String canceledAt
) {
}
