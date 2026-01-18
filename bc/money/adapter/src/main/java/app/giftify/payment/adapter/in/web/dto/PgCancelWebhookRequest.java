package app.giftify.payment.adapter.in.web.dto;

public record PgCancelWebhookRequest(
	String pgTransactionId,
	String reason,
	String canceledAt
) {
}
