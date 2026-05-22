package app.giftify.payment.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.payment.adapter.outbound.pg.TossConfirmResult;
import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.inbound.ConfirmPaymentResult;
import app.giftify.payment.application.inbound.ConfirmPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 결제 승인 UseCase 구현체.
 * PG사 결제 완료 후 Payment 상태를 PAID로 변경합니다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ConfirmPaymentService implements ConfirmPaymentUseCase {
	private static final Logger log = LoggerFactory.getLogger(ConfirmPaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentFieldEncryptor encryptor;

    private final PaymentModuleEventPublisher eventPublisher;

    @Override
    public ConfirmPaymentResult confirm(ConfirmPaymentCommand command) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new PaymentException(
                        PaymentErrorCode.PAYMENT_NOT_FOUND,
                        "[ConfirmPaymentService] 결제를 찾을 수 없습니다. paymentId=" + command.paymentId()
                ));

        // 2. 소유자 검증
        if (!payment.getMemberId().equals(command.requesterId())) {
            log.warn("[ConfirmPaymentService] 결제 소유자 불일치! paymentMemberId={}, requesterId={}",
                    payment.getMemberId(), command.requesterId());
            throw new PaymentException(PaymentErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 3. 금액 검증 (조작 방지)
        if (!payment.getPaidAmount().equals(command.requestedAmount())) {
            log.warn("[ConfirmPaymentService] 금액 불일치! expected={}, actual={}",
                    payment.getPaidAmount(), command.requestedAmount());
            throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH);
        }

        // 4. PG 승인 요청 (DB에서 조회한 금액 사용)
        TossConfirmResult pgResult = paymentGateway.confirm(
                command.paymentKey(),
                command.orderNumber(),
                payment.getPaidAmount()
        );

        if (!pgResult.success()) {
            log.warn("[ConfirmPaymentService] PG 승인 실패. paymentId={}, errorCode={}, errorMessage={}",
                    payment.getId(), pgResult.errorCode(), pgResult.errorMessage());

            Payment failed = payment.fail();
            paymentRepository.save(failed);
            eventPublisher.publishFrom(failed, payment);

            return ConfirmPaymentResult.failure(pgResult.errorCode(), pgResult.errorMessage());
        }

        // 5. 민감 정보 암호화
        String encryptedPaymentKey = encryptor.encrypt(command.paymentKey());
        LocalDateTime paidAt = LocalDateTime.now();

        // 6. 상태 변경
        Payment paid = payment.complete(
                encryptedPaymentKey,
                pgResult.approveNo(),
                pgResult.lastTransactionKey(),
                paidAt
        );

        Payment savedPayment = paymentRepository.save(paid);
        eventPublisher.publishFrom(paid, payment);

        return ConfirmPaymentResult.success(savedPayment.getId());
    }
}
