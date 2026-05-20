package app.giftify.wallet.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.*;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.*;
import app.giftify.wallet.domain.event.WalletChargedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 지갑 서비스.
 * 지갑 충전, 출금, 조회 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class WalletService implements ChargeWalletUseCase, WithdrawWalletUseCase, QueryWalletUseCase, QueryWalletHistoryUseCase, CreateWalletUseCase, RestoreWalletUseCase {
	private static final Logger log = LoggerFactory.getLogger(WalletService.class);


    private final WalletRepository walletRepository;
    private final WalletHistoryRepository historyRepository;
    private final EventPublisher eventPublisher;

    /**
     * 지갑을 충전합니다.
     * PG 결제 완료 후 지갑에 금액을 추가합니다.
     *
     * @param command 충전 요청 정보
     * @return 충전 결과
     * @throws WalletException 중복 거래이거나 충전 금액이 유효하지 않은 경우
     */
    @Override
    @Transactional
    public ChargeWalletResult charge(ChargeWalletCommand command) {
        // 1. 중복 거래 확인
        if (historyRepository.existsByReferenceIdAndReferenceType(
                command.chargeOrderId(), ReferenceType.CHARGE)) {
            throw new WalletException(
                    WalletErrorCode.DUPLICATED_TRANSACTION,
                    "[WalletService] 중복된 충전 거래입니다. chargeOrderId=" + command.chargeOrderId()
            );
        }

        // 2. 지갑 조회 또는 생성
        Wallet wallet = walletRepository.findByMemberId(command.memberId())
                .orElseGet(() -> Wallet.create(command.memberId(), Money.zero()));

        // 3. 충전
        wallet.charge(command.amount());
        Wallet savedWallet = walletRepository.save(wallet);

        // 4. 이력 기록
        historyRepository.recordTransaction(
                savedWallet.getId(),
                TransactionType.CHARGE,
                command.amount(),
                savedWallet.getBalance(),
                ReferenceType.CHARGE,
                command.chargeOrderId()
        );

        // 5. 이벤트 발행
        eventPublisher.publish(new WalletChargedEvent(
                savedWallet.getId(),
                command.memberId(),
                command.chargeOrderId(),
                command.amount(),
                savedWallet.getBalance(),
                LocalDateTime.now()
        ));

        log.info("[Wallet] 충전 완료. walletId={}, memberId={}, amount={}, balanceAfter={}",
                savedWallet.getId(), command.memberId(), command.amount(), savedWallet.getBalance());

        return new ChargeWalletResult(
                savedWallet.getId(),
                command.memberId(),
                savedWallet.getBalance(),
                command.amount(),
                command.chargeOrderId()
        );
    }

    /**
     * 지갑에서 금액을 출금합니다.
     * 출금 요청 시 잔액을 즉시 차감하고, 실제 은행 송금은 비동기로 처리됩니다.
     *
     * @param command 출금 요청 정보
     * @return 출금 결과 (PENDING 상태)
     * @throws WalletException 지갑을 찾을 수 없거나 잔액이 부족한 경우
     */
    @Override
    @Transactional
    public WithdrawWalletResult withdraw(WithdrawWalletCommand command) {
        // 1. 지갑 조회
        Wallet wallet = walletRepository.findByMemberId(command.memberId())
                .orElseThrow(() -> new WalletException(
                        WalletErrorCode.WALLET_NOT_FOUND,
                        "[WalletService] 지갑을 찾을 수 없습니다. memberId=" + command.memberId()
                ));

        // 2. 출금 (잔액 검증 포함)
        wallet.withdraw(command.amount());
        Wallet savedWallet = walletRepository.save(wallet);

        // 3. 이력 기록
        String transactionId = "WD-" + command.memberId() + "-" + System.currentTimeMillis();
        historyRepository.recordTransaction(
                savedWallet.getId(),
                TransactionType.WITHDRAW,
                command.amount(),
                savedWallet.getBalance(),
                ReferenceType.WITHDRAWAL,
                transactionId
        );

        log.info("[Wallet] 출금 요청. walletId={}, memberId={}, amount={}, balanceAfter={}, transactionId={}",
                savedWallet.getId(), command.memberId(), command.amount(), savedWallet.getBalance(), transactionId);

        // 4. 결과 반환 (실제 은행 송금은 비동기로 처리 - 현재는 PENDING 상태)
        return new WithdrawWalletResult(
                savedWallet.getId(),
                command.memberId(),
                savedWallet.getBalance(),
                command.amount(),
                transactionId,
                WithdrawStatus.PENDING
        );
    }

    /**
     * 회원의 지갑 잔액을 조회합니다.
     * 지갑이 없는 경우 잔액 0원의 새 지갑을 생성합니다.
     *
     * @param memberId 회원 ID
     * @return 지갑 잔액 정보
     */
    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResult getBalance(Long memberId) {
        Wallet wallet = walletRepository.findByMemberId(memberId)
                .orElseGet(() -> Wallet.create(memberId, Money.zero()));

        return new WalletBalanceResult(
                wallet.getId(),
                memberId,
                wallet.getBalance()
        );
    }


    /**
     * 회원의 지갑 거래 내역을 조회합니다.
     *
     * @param query 조회 조건 (memberId, type, page, size)
     * @return 페이징된 거래 내역
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WalletHistoryResult> getHistory(WalletHistoryQuery query) {
        Wallet wallet = walletRepository.findByMemberId(query.memberId())
                .orElseGet(() -> Wallet.create(query.memberId(), Money.zero()));

        if (wallet.getId() == null) {
            // 지갑이 없으면 빈 결과 반환
            return Page.empty(query.toPageable());
        }

        return historyRepository.findByWalletId(
                wallet.getId(),
                query.type(),
                query.toPageable()
        ).map(WalletHistoryResult::from);
    }

    /**
     * 회원의 지갑을 생성합니다.
     * 이미 지갑이 존재하는 경우 기존 지갑 정보를 반환합니다.
     *
     * @param memberId 회원 ID
     * @return 생성 결과
     */
    @Override
    @Transactional
    public CreateWalletResult createIfNotExists(Long memberId) {
        return walletRepository.findByMemberId(memberId)
                .map(wallet -> {
                    log.debug("[WalletService] 이미 지갑이 존재합니다. memberId={}, walletId={}", memberId, wallet.getId());
                    return CreateWalletResult.alreadyExists(wallet.getId(), memberId);
                })
                .orElseGet(() -> {
                    Wallet wallet = Wallet.create(memberId, Money.zero());
                    Wallet savedWallet = walletRepository.save(wallet);
                    log.info("[WalletService] 신규 지갑 생성 완료. memberId={}, walletId={}", memberId, savedWallet.getId());
                    return CreateWalletResult.created(savedWallet.getId(), memberId);
                });
    }

    @Override
    @Transactional
    public void restore(RestoreWalletCommand command) {
        if (historyRepository.existsByReferenceIdAndReferenceType(command.referenceId(), command.referenceType())) {
            log.warn("[WalletService] 중복 취소 환불 요청. referenceId={}", command.referenceId());
            return;
        }

        Wallet wallet = walletRepository.findByMemberId(command.memberId())
                .orElseThrow(() -> new WalletException(
                        WalletErrorCode.WALLET_NOT_FOUND,
                        "[WalletService] 지갑을 찾을 수 없습니다. memberId=" + command.memberId()
                ));

        wallet.restoreForCancel(command.amount());
        Wallet savedWallet = walletRepository.save(wallet);

        historyRepository.recordTransaction(
                savedWallet.getId(),
                command.transactionType(),
                command.amount(),
                savedWallet.getBalance(),
                command.referenceType(),
                command.referenceId()
        );

        log.info("[Wallet] 취소 환불 완료. walletId={}, memberId={}, amount={}, balanceAfter={}",
                savedWallet.getId(), command.memberId(), command.amount(), savedWallet.getBalance());
    }
}
