package app.giftify.payment.application;

import app.giftify.payment.application.inbound.*;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.application.strategy.PaymentCreateStrategy;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentCreateContext;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class CreatePaymentService implements ChargeDepositUseCase, CreatePaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final List<PaymentCreateStrategy> strategies;

    public CreatePaymentService(PaymentRepository paymentRepository, List<PaymentCreateStrategy> strategies) {
        this.paymentRepository = paymentRepository;
        this.strategies = strategies;
    }

    //--------- ChargeDepositUseCase 구현 - 예치금 충전 ----------//

    @Override
    public PaymentCreatedResult charge(ChargeDepositCommand command) {
        return paymentRepository.findByOrderNumber(command.orderNumber())
                .map(PaymentCreatedResult::from)
                .orElseGet(() -> createDepositChargePayment(command));
    }

    private PaymentCreatedResult createDepositChargePayment(ChargeDepositCommand command) {
        PaymentCreateContext context = new PaymentCreateContext(
                command.memberId(),
                null,
                command.orderNumber(),
                PaymentType.DEPOSIT_CHARGE,
                PaymentMethod.CARD
        );

        Payment payment = Payment.createForDepositCharge(context, command.amount());
        Payment savedPayment = paymentRepository.save(payment);

        log.info("[Payment] 예치금 충전 결제 생성. paymentId={}, orderId={}, amount={}",
                savedPayment.getId(), savedPayment.getOrderNumber(), command.amount());

        return PaymentCreatedResult.from(savedPayment);
    }

    //--------- CreateFundingPaymentUseCase 구현 - 펀딩 결제 ----------//

    @Override
    public PaymentCreatedResult create(CreatePaymentCommand command) {
        return paymentRepository.findByOrderNumber(command.orderNumber())
                .map(PaymentCreatedResult::from)
                .orElseGet(() -> createPayment(command));
    }

    private PaymentCreatedResult createPayment(CreatePaymentCommand command) {
        PaymentCreateContext context = new PaymentCreateContext(
                command.memberId(),
                command.orderId(),
                command.orderNumber(),
                command.getType(),
                command.method()
        );

        Payment payment = Payment.create(
                context,
                command.expectedAmount(),
                command.expectedAmount(),
                command.walletDeductAmount(),
                command.orderItems()
        );

        Payment savedPayment = paymentRepository.save(payment);

        PaymentCreateStrategy strategy = strategies.stream()
                .filter(s -> s.canHandle(command))
                .findFirst()
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                        "[Payment] No strategy for method=" + command.method()
                                + ", walletDeduct=" + command.walletDeductAmount()));

        log.debug("[Payment] Strategy selected: {} for method={}, walletDeduct={}",
                strategy.getClass().getSimpleName(), command.method(), command.walletDeductAmount());

        return strategy.execute(savedPayment, command);
    }
}
