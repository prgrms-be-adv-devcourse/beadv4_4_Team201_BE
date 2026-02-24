package app.giftify.payment.domain;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.vo.Money;

import static org.assertj.core.api.Assertions.assertThat;

class CancelTest {

	@Test
	void create_shouldSetFieldsCorrectly() {
		var now = LocalDateTime.now();
		var cancel = Cancel.create(
			1L, "internal-uuid-123", Money.of(3000), "주문 취소", now
		);

		assertThat(cancel.getId()).isNull();
		assertThat(cancel.getPaymentId()).isEqualTo(1L);
		assertThat(cancel.getTransactionKey()).isEqualTo("internal-uuid-123");
		assertThat(cancel.getCancelAmount()).isEqualTo(Money.of(3000));
		assertThat(cancel.getCancelReason()).isEqualTo("주문 취소");
		assertThat(cancel.getCanceledAt()).isEqualTo(now);
	}
}
