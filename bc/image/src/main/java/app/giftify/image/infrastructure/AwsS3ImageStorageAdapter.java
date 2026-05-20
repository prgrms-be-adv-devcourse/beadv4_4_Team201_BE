package app.giftify.image.infrastructure;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import app.giftify.image.application.port.out.ImageStoragePort;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class AwsS3ImageStorageAdapter implements ImageStoragePort {

	private final S3Presigner presigner;
	private final String bucket;

	public AwsS3ImageStorageAdapter(
		S3Presigner presigner,
		@Value("${giftify.storage.s3.image-bucket}") String bucket
	) {
		if (!StringUtils.hasText(bucket)) {
			throw new IllegalArgumentException("image bucket must not be blank");
		}
		this.presigner = presigner;
		this.bucket = bucket;
	}

	@Override
	public PresignedUrl presignPut(String key, String contentType, Duration expiry) {
		PutObjectRequest put = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType(contentType)
			.build();
		PutObjectPresignRequest req = PutObjectPresignRequest.builder()
			.signatureDuration(expiry)
			.putObjectRequest(put)
			.build();
		PresignedPutObjectRequest signed = presigner.presignPutObject(req);
		return new PresignedUrl(signed.url().toString(), "PUT", expiry);
	}

	@Override
	public PresignedUrl presignGet(String key, Duration expiry) {
		GetObjectRequest get = GetObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();
		GetObjectPresignRequest req = GetObjectPresignRequest.builder()
			.signatureDuration(expiry)
			.getObjectRequest(get)
			.build();
		PresignedGetObjectRequest signed = presigner.presignGetObject(req);
		return new PresignedUrl(signed.url().toString(), "GET", expiry);
	}
}
