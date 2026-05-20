package app.giftify.image.application.service;

import java.util.Set;
import java.util.UUID;

public final class ImageKeyGenerator {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"image/jpeg", "image/png", "image/webp", "image/gif"
	);

	private ImageKeyGenerator() {
	}

	public static String generate(String domain, Long ownerId, String contentType) {
		if (domain == null || domain.isBlank()) {
			throw new IllegalArgumentException("domain must not be blank");
		}
		if (ownerId == null) {
			throw new IllegalArgumentException("ownerId must not be null");
		}
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new IllegalArgumentException("unsupported contentType: " + contentType);
		}
		String ext = switch (contentType) {
			case "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			case "image/gif" -> "gif";
			default -> throw new IllegalStateException();
		};
		return "%s/%d/%s.%s".formatted(domain, ownerId, UUID.randomUUID(), ext);
	}
}
