package app.giftify.notification.application;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public record CloudEventEnvelope(
	String specVersion,
	String id,
	String source,
	String type,
	String subject,
	String time,
	String dataContentType,
	Object data
) {
	private static final String SPEC_VERSION = "1.0";

	public static CloudEventEnvelope of(
		String id, String source, String type,
		String subject, OffsetDateTime time
	) {
		return new CloudEventEnvelope(
			SPEC_VERSION, id, source, type, subject,
			time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			null, null
		);
	}

	public static CloudEventEnvelope withData(
		String id, String source, String type,
		String subject, OffsetDateTime time,
		Map<String, Object> data
	) {
		return new CloudEventEnvelope(
			SPEC_VERSION, id, source, type, subject,
			time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			"application/json", data
		);
	}
}
