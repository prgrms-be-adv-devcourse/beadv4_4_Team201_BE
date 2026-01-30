package app.giftify.settlement.domain;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.DomainException;
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
public class LifeCycleMeta {
        @Enumerated(EnumType.STRING)
        private SettlementItemStatus status;
        private LocalDate expectedDate;
        private LocalDateTime settledAt;
        private LocalDateTime cancelledAt;

        private LifeCycleMeta(SettlementItemStatus status, LocalDate expectedDate, LocalDateTime settledAt, LocalDateTime cancelledAt) {
                if (status == null || expectedDate == null) {
                        throw new DomainException(SettlementErrorCode.INVALID_LIFECYCLE_META);
                }

                this.status = status;
                this.expectedDate = expectedDate;
                this.settledAt = settledAt;
                this.cancelledAt = cancelledAt;
        }

        public static LifeCycleMeta ready(LocalDateTime confirmedAt) {
                if (confirmedAt == null) {
                        throw new DomainException(SettlementErrorCode.INVALID_LIFECYCLE_META);
                }

                return new LifeCycleMeta(
                        SettlementItemStatus.READY,
                        calculateExpectedDate(confirmedAt),
                        null,
                        null
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


