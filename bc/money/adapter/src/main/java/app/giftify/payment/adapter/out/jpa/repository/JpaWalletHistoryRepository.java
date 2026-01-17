package app.giftify.payment.adapter.out.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWalletHistoryRepository extends JpaRepository<JpaWalletHistory, Long> {
}
