package app.giftify.notification.application.outbound;

import app.giftify.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
	Notification save(Notification notification);
	List<Notification> saveAll(List<Notification> notifications);
	Optional<Notification> findById(Long id);
	Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);
	Page<Notification> findByRecipientIdAndIsReadFalse(Long recipientId, Pageable pageable);
	long countByRecipientIdAndIsReadFalse(Long recipientId);
	void markAllAsReadByRecipientId(Long recipientId);
}
