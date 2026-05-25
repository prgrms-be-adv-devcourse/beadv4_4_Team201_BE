package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.support.common.api.exception.DomainException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor
@Getter
public class ItemStatusInfo {
        @Enumerated(EnumType.STRING)
        private SettlementItemStatus status;
        private LocalDate expectedDate;
        private LocalDateTime settledAt;
        private LocalDateTime cancelledAt;

        private ItemStatusInfo(SettlementItemStatus status, LocalDate expectedDate, LocalDateTime settledAt, LocalDateTime cancelledAt) {
                if (status == null || expectedDate == null) {
                        throw new DomainException(SettlementErrorCode.INVALID_LIFECYCLE_META);
                }

                this.status = status;
                this.expectedDate = expectedDate;
                this.settledAt = settledAt;
                this.cancelledAt = cancelledAt;
        }

        public static ItemStatusInfo create(LocalDateTime confirmedAt) {
                if (confirmedAt == null) {
                        throw new DomainException(SettlementErrorCode.INVALID_LIFECYCLE_META);
                }

                return new ItemStatusInfo(
                        SettlementItemStatus.CREATED,
                        calculateExpectedDate(confirmedAt),
                        null,
                        null
                );
        }

        public ItemStatusInfo validating() {
                if (this.status != SettlementItemStatus.CREATED) {
                        throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
                }

                return new ItemStatusInfo(
                        SettlementItemStatus.VALIDATING,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }


        public ItemStatusInfo failToValidate() {
                return new ItemStatusInfo(
                        SettlementItemStatus.VALIDATE_FAILED,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        public ItemStatusInfo ready() {
                return new ItemStatusInfo(
                        SettlementItemStatus.READY,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        public ItemStatusInfo processing() {
                if (this.status != SettlementItemStatus.READY) {
                        throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
                }
                return new ItemStatusInfo(
                        SettlementItemStatus.PROCESSING,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        public ItemStatusInfo failToExecute() {
                return new ItemStatusInfo(
                        SettlementItemStatus.FAILED,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        public ItemStatusInfo complete() {
                if (this.status != SettlementItemStatus.PROCESSING) {
                        throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
                }

                return new ItemStatusInfo(
                        SettlementItemStatus.COMPLETED,
                        expectedDate,
                        LocalDateTime.now(),
                        cancelledAt
                );
        }

        public ItemStatusInfo manual() {
                return new ItemStatusInfo(
                        SettlementItemStatus.MANUAL,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        public ItemStatusInfo cancel() {
                return new ItemStatusInfo(
                        SettlementItemStatus.CANCELLED,
                        expectedDate,
                        settledAt,
                        LocalDateTime.now()
                );
        }

        // 익월 정산
        private static LocalDate calculateExpectedDate(LocalDateTime confirmedAt) {
                return confirmedAt.toLocalDate()
                        .withDayOfMonth(1)
                        .plusMonths(1); // 다음 달 1일에 정산 예정
        }
}


