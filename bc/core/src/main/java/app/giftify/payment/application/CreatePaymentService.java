package app.giftify.payment.application;

import app.giftify.payment.application.inbound.*;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.*;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CreatePaymentService implements ChargeDepositUseCase, CreateFundingPaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final DeductWalletUseCase deductWalletUseCase;

    public CreatePaymentService(PaymentRepository paymentRepository, DeductWalletUseCase deductWalletUseCase) {
        this.paymentRepository = paymentRepository;
        this.deductWalletUseCase = deductWalletUseCase;
    }

    //--------- ChargeDepositUseCase 구현 - 예치금 충전 ----------//

    @Override
    public PaymentCreatedResult charge(ChargeDepositCommand command) {
        return paymentRepository.findByOrderNumber(command.orderNumber())
                .map(existing -> new PaymentCreatedResult(
                        existing.getId(),
                        existing.getOrderNumber(),
                        existing.getStatus(),
                        existing.getPaymentKey(),
                        existing.getLastTransactionKey(),
                        existing.getCreatedAt()
                ))
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

        return new PaymentCreatedResult(
                savedPayment.getId(),
                savedPayment.getOrderNumber(),
                savedPayment.getStatus(),
                savedPayment.getPaymentKey(),
                savedPayment.getLastTransactionKey(),
                savedPayment.getCreatedAt()
        );
    }

    //--------- CreateFundingPaymentUseCase 구현 - 펀딩 결제 ----------//

    @Override
    public PaymentCreatedResult create(CreateFundingPaymentCommand command) {
        return paymentRepository.findByOrderNumber(command.orderNumber())
                .map(existing -> new PaymentCreatedResult(
                        existing.getId(),
                        existing.getOrderNumber(),
                        existing.getStatus(),
                        existing.getPaymentKey(),
                        existing.getLastTransactionKey(),
                        existing.getCreatedAt()
                ))
                .orElseGet(() -> createFundingPayment(command));
    }

    private PaymentCreatedResult createFundingPayment(CreateFundingPaymentCommand command) {
        // PaymentCreateContext 생성
        PaymentCreateContext context = new PaymentCreateContext(
                command.memberId(),
                command.orderId(),
                command.orderNumber(),
                command.getType(),
                command.method()
        );

        Payment payment = Payment.createForFunding(
                context,
                command.expectedAmount(),
                command.expectedAmount(),
                command.walletDeductAmount(),
                command.orderItems()
        );

        Payment savedPayment = paymentRepository.save(payment);

        // 내부 지갑 결제(예치금)면 즉시 처리
        if (command.method().isWalletPayment()) {
            return handleWalletPaymentForFunding(savedPayment, command);
        }

        // 복합결제: walletDeductAmount > 0 이고 PG 결제 수단
        if (command.walletDeductAmount().isGreaterThan(Money.zero())) {
            // pgAmount==0이면 기존 Wallet 흐름으로 위임
            if (command.walletDeductAmount().equals(command.expectedAmount())) {
                return handleWalletPaymentForFunding(savedPayment, command);
            }
            return handleCompositePaymentForFunding(savedPayment, command);
        }


        log.info("[Payment] 펀딩 결제 생성. paymentId={}, orderId={}, amount={}",
                savedPayment.getId(), savedPayment.getOrderNumber(), command.expectedAmount());

        return new PaymentCreatedResult(
                savedPayment.getId(),
                savedPayment.getOrderNumber(),
                savedPayment.getStatus(),
                savedPayment.getPaymentKey(),
                savedPayment.getLastTransactionKey(),
                savedPayment.getCreatedAt()
        );
    }

    private PaymentCreatedResult handleWalletPaymentForFunding(Payment payment, CreateFundingPaymentCommand command) {
        DeductWalletCommand deductCommand = new DeductWalletCommand(
                command.memberId(),
                payment.getId(),
                command.orderNumber(),
                command.expectedAmount()
        );

        DeductWalletResult result = deductWalletUseCase.deductForPayment(deductCommand);

        if (!result.success()) {
            log.warn("[Payment] 예치금 잔액 부족. paymentId={}, required={}, current={}",
                    payment.getId(), result.requiredAmount(), result.currentBalance());

            throw new PaymentException(
                    PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE,
                    String.format("필요 금액: %s, 현재 잔액: %s",
                            result.requiredAmount(), result.currentBalance())
            );
        }

        log.info("[Payment] 예치금 결제 요청 완료. paymentId={}, walletId={}",
                payment.getId(), result.walletId());

        return new PaymentCreatedResult(
                payment.getId(),
                payment.getOrderNumber(),
                PaymentStatus.PENDING,
                payment.getPaymentKey(),
                payment.getLastTransactionKey(),
                payment.getCreatedAt()
        );
    }

    private PaymentCreatedResult handleCompositePaymentForFunding(Payment payment, CreateFundingPaymentCommand command) {
        DeductWalletCommand deductCommand = new DeductWalletCommand(
                command.memberId(),
                payment.getId(),
                command.orderNumber(),
                command.walletDeductAmount()
        );

        DeductWalletResult result = deductWalletUseCase.deductForPayment(deductCommand);

        if (!result.success()) {
            log.warn("[Payment] 복합결제 예치금 잔액 부족. paymentId={}, required={}, current={}",
                    payment.getId(), result.requiredAmount(), result.currentBalance());

            throw new PaymentException(
                    PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE,
                    String.format("필요 금액: %s, 현재 잔액: %s",
                            result.requiredAmount(), result.currentBalance())
            );
        }

        log.info("[Payment] 복합결제 예치금 차감 완료. paymentId={}, walletDeduct={}, pgAmount={}",
                payment.getId(), command.walletDeductAmount(),
                command.expectedAmount().minus(command.walletDeductAmount()));

        return new PaymentCreatedResult(
                payment.getId(),
                payment.getOrderNumber(),
                PaymentStatus.PENDING,
                payment.getPaymentKey(),
                payment.getLastTransactionKey(),
                payment.getCreatedAt()
        );
    }

}