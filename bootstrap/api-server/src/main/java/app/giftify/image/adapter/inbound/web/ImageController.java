package app.giftify.image.adapter.inbound.web;

import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.image.application.port.in.IssuePresignedDownloadUseCase;
import app.giftify.image.application.port.in.IssuePresignedUploadUseCase;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.support.common.api.response.RsData;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v2/images")
public class ImageController {

	private static final Duration UPLOAD_EXPIRY = Duration.ofMinutes(5);
	private static final Duration DOWNLOAD_EXPIRY = Duration.ofMinutes(10);

	private final IssuePresignedUploadUseCase uploadUseCase;
	private final IssuePresignedDownloadUseCase downloadUseCase;

	public ImageController(
		IssuePresignedUploadUseCase uploadUseCase,
		IssuePresignedDownloadUseCase downloadUseCase
	) {
		this.uploadUseCase = uploadUseCase;
		this.downloadUseCase = downloadUseCase;
	}

	@PostMapping("/presigned-upload")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<RsData<PresignedUrlResponseDto>> presignedUpload(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PresignedUploadRequestDto request
	) {
		var command = new IssuePresignedUploadUseCase.Command(
			request.domain(), memberId, request.contentType(), UPLOAD_EXPIRY);
		var issued = uploadUseCase.issue(command);
		var body = PresignedUrlResponseDto.of(issued.key(), issued.url(), issued.httpMethod(), issued.expiry());
		return ResponseEntity.ok(RsData.success(body));
	}

	@GetMapping("/presigned-download")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<RsData<PresignedUrlResponseDto>> presignedDownload(
		@RequestParam("key") String key
	) {
		var command = new IssuePresignedDownloadUseCase.Command(key, DOWNLOAD_EXPIRY);
		var issued = downloadUseCase.issue(command);
		var body = PresignedUrlResponseDto.of(issued.key(), issued.url(), issued.httpMethod(), issued.expiry());
		return ResponseEntity.ok(RsData.success(body));
	}
}
