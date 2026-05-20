package app.giftify.image.application.port.out;

import java.time.Duration;

public interface ImageStoragePort {

	PresignedUrl presignPut(String key, String contentType, Duration expiry);

	PresignedUrl presignGet(String key, Duration expiry);

	record PresignedUrl(
		String url,
		String httpMethod,
		Duration expiry
	) {
	}
}
