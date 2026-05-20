package app.giftify.image.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.image.application.port.in.IssuePresignedDownloadUseCase;
import app.giftify.image.application.port.in.IssuePresignedUploadUseCase;
import app.giftify.image.application.port.out.ImageStoragePort;
import app.giftify.image.application.port.out.ImageStoragePort.PresignedUrl;

@Service
public class ImageStorageService implements IssuePresignedUploadUseCase, IssuePresignedDownloadUseCase {

	private final ImageStoragePort storage;

	public ImageStorageService(ImageStoragePort storage) {
		this.storage = storage;
	}

	@Override
	@Transactional(readOnly = true)
	public PresignedUpload issue(IssuePresignedUploadUseCase.Command command) {
		String key = ImageKeyGenerator.generate(command.domain(), command.ownerId(), command.contentType());
		PresignedUrl url = storage.presignPut(key, command.contentType(), command.expiry());
		return new PresignedUpload(key, url.url(), url.httpMethod(), url.expiry());
	}

	@Override
	@Transactional(readOnly = true)
	public PresignedDownload issue(IssuePresignedDownloadUseCase.Command command) {
		PresignedUrl url = storage.presignGet(command.key(), command.expiry());
		return new PresignedDownload(command.key(), url.url(), url.httpMethod(), url.expiry());
	}
}
