package app.giftify.image.adapter.inbound.web;

import java.time.Duration;

public record PresignedUrlResponseDto(
	String key,
	String url,
	String httpMethod,
	long expiresInSeconds
) {
	public static PresignedUrlResponseDto of(String key, String url, String httpMethod, Duration expiry) {
		return new PresignedUrlResponseDto(key, url, httpMethod, expiry.toSeconds());
	}
}
