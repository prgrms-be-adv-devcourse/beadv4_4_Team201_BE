package app.giftify.image.application.port.in;

import java.time.Duration;

public interface IssuePresignedUploadUseCase {

	PresignedUpload issue(Command command);

	record Command(
		String domain,
		Long ownerId,
		String contentType,
		Duration expiry
	) {
		public Command {
			if (domain == null || domain.isBlank()) {
				throw new IllegalArgumentException("domain must not be blank");
			}
			if (ownerId == null) {
				throw new IllegalArgumentException("ownerId must not be null");
			}
			if (contentType == null || contentType.isBlank()) {
				throw new IllegalArgumentException("contentType must not be blank");
			}
			if (expiry == null || expiry.isNegative() || expiry.isZero()) {
				throw new IllegalArgumentException("expiry must be positive");
			}
		}
	}

	record PresignedUpload(
		String key,
		String url,
		String httpMethod,
		Duration expiry
	) {
	}
}
