package app.giftify.settlement.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
public record LifeCycleMeta(
        @Enumerated(EnumType.STRING)
        SettlementItemStatus status,
        LocalDate expectedDate,
        LocalDateTime settledAt,
        LocalDateTime cancelledAt
) {
        public LifeCycleMeta {
                if (status == null) throw new RuntimeException();
                if (expectedDate == null) throw new RuntimeException();
                if (settledAt == null) throw new RuntimeException();
                if (cancelledAt == null) throw new RuntimeException();
        }

        public static LifeCycleMeta of(LocalDateTime confirmedAt) {
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
}


