package app.giftify.image.application.port.in;

import java.time.Duration;

public interface IssuePresignedDownloadUseCase {

	PresignedDownload issue(Command command);

	record Command(
		String key,
		Duration expiry
	) {
		public Command {
			if (key == null || key.isBlank()) {
				throw new IllegalArgumentException("key must not be blank");
			}
			if (expiry == null || expiry.isNegative() || expiry.isZero()) {
				throw new IllegalArgumentException("expiry must be positive");
			}
		}
	}

	record PresignedDownload(
		String key,
		String url,
		String httpMethod,
		Duration expiry
	) {
	}
}
