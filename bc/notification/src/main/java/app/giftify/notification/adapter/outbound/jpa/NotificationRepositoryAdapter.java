package app.giftify.notification.adapter.outbound.jpa;

import app.giftify.notification.application.outbound.NotificationRepository;
import app.giftify.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

	private final JpaNotificationRepository jpaRepository;

	@Override
	public Notification save(Notification notification) {
		return jpaRepository.save(notification);
	}

	@Override
	public List<Notification> saveAll(List<Notification> notifications) {
		return jpaRepository.saveAll(notifications);
	}

	@Override
	public Optional<Notification> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Page<Notification> findByRecipientId(Long recipientId, Pageable pageable) {
		return jpaRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
	}

	@Override
	public Page<Notification> findByRecipientIdAndIsReadFalse(Long recipientId, Pageable pageable) {
		return jpaRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId, pageable);
	}

	@Override
	public long countByRecipientIdAndIsReadFalse(Long recipientId) {
		return jpaRepository.countByRecipientIdAndIsReadFalse(recipientId);
	}

	@Override
	@Transactional
	public void markAllAsReadByRecipientId(Long recipientId) {
		jpaRepository.markAllAsReadByRecipientId(recipientId, LocalDateTime.now());
	}
}
