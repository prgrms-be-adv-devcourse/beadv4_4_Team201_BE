package app.giftify.payment.application;

import static app.giftify.payment.domain.SystemConstants.*;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.adapter.outbound.pg.TossCancelResult;
import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.payment.application.outbound.CancelRepository;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Cancel;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.vo.Money;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CancelPaymentService implements CancelPaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final CancelRepository cancelRepository;
	private final PaymentGateway paymentGateway;
	private final EventPublisher eventPublisher;
	private final PaymentFieldEncryptor encryptor;

	public CancelPaymentService(
		PaymentRepository paymentRepository, CancelRepository cancelRepository,
		PaymentGateway paymentGateway, EventPublisher eventPublisher,
		PaymentFieldEncryptor encryptor
	) {
		this.paymentRepository = paymentRepository;
		this.cancelRepository = cancelRepository;
		this.paymentGateway = paymentGateway;
		this.eventPublisher = eventPublisher;
		this.encryptor = encryptor;
	}

	@Override
	public void cancel(CancelPaymentCommand command) {
		Payment payment = paymentRepository.findById(command.paymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[CancelPaymentService] 결제를 찾을 수 없습니다. paymentId=" + command.paymentId()
			));

		if (!Objects.equals(command.requesterId(), SYSTEM_REQUESTER_ID)
			&& !payment.isOwnedBy(command.requesterId())) {
			throw new PaymentException(
				PaymentErrorCode.UNAUTHORIZED_ACCESS,
				"[CancelPaymentService] 결제 취소 권한이 없습니다. requesterId=" + command.requesterId()
			);
		}

		if (command.cancelAmount() != null) {
			handlePartialCancel(payment, command);
		} else {
			handleFullCancel(payment, command);
		}
	}

	private void handleFullCancel(Payment payment, CancelPaymentCommand command) {
		CancelType cancelType = payment.resolveCancelType();

		if (cancelType == CancelType.REFUND) {
			TossCancelResult pgResult = callPgCancel(payment, command.reason(), null);
			if (pgResult == null) return;

			payment.markAsCanceled(cancelType, command.reason());
			saveCancelRecord(payment, pgResult.lastTransactionKey(), payment.getPaidAmount(), command.reason());
		} else {
			payment.markAsCanceled(cancelType, command.reason());
		}

		saveAndPublish(payment);
	}

	private void handlePartialCancel(Payment payment, CancelPaymentCommand command) {
		if (payment.resolveCancelType() != CancelType.REFUND) {
			throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
				"[CancelPaymentService] 부분 취소 불가능한 상태입니다: " + payment.getStatus());
		}

		Money cancelAmount = command.cancelAmount();
		TossCancelResult pgResult = callPgCancel(payment, command.reason(), cancelAmount);
		if (pgResult == null) return;

		payment.markAsPartiallyCanceled(
			pgResult.lastTransactionKey(), cancelAmount, CancelType.REFUND, command.reason()
		);
		saveCancelRecord(payment, pgResult.lastTransactionKey(), cancelAmount, command.reason());

		saveAndPublish(payment);
	}

	private TossCancelResult callPgCancel(Payment payment, String reason, Money cancelAmount) {
		String decryptedPaymentKey = encryptor.decrypt(payment.getPaymentKey());
		TossCancelResult pgResult = paymentGateway.cancel(decryptedPaymentKey, reason, cancelAmount);

		if (!pgResult.success()) {
			payment.recordCancelFailed(pgResult.errorMessage(), LocalDateTime.now());
			var failEvents = payment.pullEvents();
			paymentRepository.save(payment);
			failEvents.forEach(eventPublisher::publish);
			return null;
		}
		return pgResult;
	}

	private void saveCancelRecord(Payment payment, String transactionKey, Money cancelAmount, String reason) {
		Cancel cancel = Cancel.create(
			payment.getId(), transactionKey, cancelAmount, reason, LocalDateTime.now()
		);
		cancelRepository.save(cancel);
	}

	private void saveAndPublish(Payment payment) {
		var domainEvents = payment.pullEvents();
		paymentRepository.save(payment);
		domainEvents.forEach(eventPublisher::publish);
	}
}
