package app.giftify.payment.application;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

import static app.giftify.payment.domain.SystemConstants.SYSTEM_REQUESTER_ID;

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

		Payment canceled;
		if (cancelType == CancelType.REFUND) {
			TossCancelResult pgResult = callPgCancel(payment, command.reason(), null);
			if (pgResult == null) return;

			canceled = payment.cancel(cancelType, command.reason());
			saveCancelRecord(canceled, pgResult.lastTransactionKey(), canceled.getPaidAmount(), command.reason());
		} else {
			canceled = payment.cancel(cancelType, command.reason()); // PG 취소 불필요한 경우 (예: WALLET) 바로 상태 변경
		}

		saveAndPublish(canceled);
	}

	private void handlePartialCancel(Payment payment, CancelPaymentCommand command) {
		if (payment.resolveCancelType() != CancelType.REFUND) {
			throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
					"[CancelPaymentService] 부분 취소 불가능한 상태입니다: " + payment.getStatus());
		}

		Money cancelAmount = command.cancelAmount();
		TossCancelResult pgResult = callPgCancel(payment, command.reason(), cancelAmount);
		if (pgResult == null) return;

		Payment partiallyCanceled = payment.partialCancel(
				pgResult.lastTransactionKey(), cancelAmount, CancelType.REFUND, command.reason()
		);
		saveCancelRecord(partiallyCanceled, pgResult.lastTransactionKey(), cancelAmount, command.reason());

		saveAndPublish(partiallyCanceled);
	}

	private TossCancelResult callPgCancel(Payment payment, String reason, Money cancelAmount) {
		String decryptedPaymentKey = encryptor.decrypt(payment.getPaymentKey());
		TossCancelResult pgResult = paymentGateway.cancel(decryptedPaymentKey, reason, cancelAmount);

		if (!pgResult.success()) {
			Payment cancelFailed = payment.failCancel(pgResult.errorMessage());
			var failEvents = cancelFailed.pullEvents();
			paymentRepository.save(cancelFailed);
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
