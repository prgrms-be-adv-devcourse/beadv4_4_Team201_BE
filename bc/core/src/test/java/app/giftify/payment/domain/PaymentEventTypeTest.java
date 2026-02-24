package app.giftify.payment.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentEventTypeTest {

	@Test
	void partialCancel_fromPaid_shouldTransitionToPartiallyCanceled() {
		assertThat(PaymentEventType.PARTIAL_CANCEL.canApply(PaymentStatus.PAID)).isTrue();
		assertThat(PaymentEventType.PARTIAL_CANCEL.getResultStatus())
			.isEqualTo(PaymentStatus.PARTIALLY_CANCELED);
	}

	@Test
	void partialCancelAgain_fromPartiallyCanceled_shouldStayPartiallyCanceled() {
		assertThat(PaymentEventType.PARTIAL_CANCEL_AGAIN
			.canApply(PaymentStatus.PARTIALLY_CANCELED)).isTrue();
		assertThat(PaymentEventType.PARTIAL_CANCEL_AGAIN.getResultStatus())
			.isEqualTo(PaymentStatus.PARTIALLY_CANCELED);
	}

	@Test
	void finalCancel_fromPartiallyCanceled_shouldTransitionToCanceled() {
		assertThat(PaymentEventType.FINAL_CANCEL
			.canApply(PaymentStatus.PARTIALLY_CANCELED)).isTrue();
		assertThat(PaymentEventType.FINAL_CANCEL.getResultStatus())
			.isEqualTo(PaymentStatus.CANCELED);
	}

	@Test
	void partialCancel_fromPending_shouldNotApply() {
		assertThat(PaymentEventType.PARTIAL_CANCEL.canApply(PaymentStatus.PENDING)).isFalse();
	}
}
