package app.giftify.notification.adapter.outbound.jpa;

import app.giftify.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface JpaNotificationRepository extends JpaRepository<Notification, Long> {
	Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
	Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
	long countByRecipientIdAndIsReadFalse(Long recipientId);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.recipientId = :recipientId AND n.isRead = false")
	void markAllAsReadByRecipientId(@Param("recipientId") Long recipientId, @Param("now") LocalDateTime now);
}
