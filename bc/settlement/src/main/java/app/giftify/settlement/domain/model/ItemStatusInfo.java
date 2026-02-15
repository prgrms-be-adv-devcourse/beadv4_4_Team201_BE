package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.api.exception.DomainException;
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

        public static ItemStatusInfo pending(LocalDateTime confirmedAt) {
                if (confirmedAt == null) {
                        throw new DomainException(SettlementErrorCode.INVALID_LIFECYCLE_META);
                }

                return new ItemStatusInfo(
                        SettlementItemStatus.PENDING,
                        calculateExpectedDate(confirmedAt),
                        null,
                        null
                );
        }

        public ItemStatusInfo ready() {
                if (this.status != SettlementItemStatus.PENDING) {
                        throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
                }

                return new ItemStatusInfo(
                        SettlementItemStatus.READY,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        public ItemStatusInfo fail() {
                return new ItemStatusInfo(
                        SettlementItemStatus.FAIL,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        public ItemStatusInfo manual() {
                return new ItemStatusInfo(
                        SettlementItemStatus.MANUAL_CHECK,
                        expectedDate,
                        settledAt,
                        cancelledAt
                );
        }

        // 익월 정산
        private static LocalDate calculateExpectedDate(LocalDateTime confirmedAt) {
                return confirmedAt.toLocalDate()
                        .withDayOfMonth(1)
                        .plusMonths(1); // 다음 달 1일에 정산 예정
        }

// todo: 추후 기능 추가

//        public LifeCycleMeta start() {
//                if (this.status != SettlementItemStatus.READY) {
//                        throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
//                }
//                return new LifeCycleMeta(SettlementItemStatus.IN_PROGRESS, expectedDate, null, null);
//        }
//
//        public LifeCycleMeta complete(LocalDateTime settledAt) {
//                if (this.status != SettlementItemStatus.IN_PROGRESS) {
//                        throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
//                }
//                if (settledAt == null) {
//                        throw new DomainException(SettlementErrorCode.INVALID_LIFECYCLE_META);
//                }
//                return new LifeCycleMeta(SettlementItemStatus.COMPLETED, expectedDate, settledAt, cancelledAt);
//        }
//
//        public LifeCycleMeta cancel(LocalDateTime cancelledAt) {
//                if (this.status == SettlementItemStatus.COMPLETED) {
//                        throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
//                }
//                return new LifeCycleMeta(SettlementItemStatus.CANCELLED, expectedDate, settledAt, cancelledAt);
//        }
}


