package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.api.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemStatusInfoTest {

    @Test
    @DisplayName("정상 생성 시 CREATED 상태이고, expectedDate는 다음달 1일이다")
    void create_withValidConfirmedAt_returnsCreatedStatus() {
        // given
        LocalDateTime confirmedAt = LocalDateTime.of(2025, 3, 15, 10, 0);

        // when
        ItemStatusInfo statusInfo = ItemStatusInfo.create(confirmedAt);

        // then
        assertThat(statusInfo.getStatus()).isEqualTo(SettlementItemStatus.CREATED);
        assertThat(statusInfo.getExpectedDate()).isEqualTo(LocalDate.of(2025, 4, 1));
        assertThat(statusInfo.getSettledAt()).isNull();
        assertThat(statusInfo.getCancelledAt()).isNull();
    }

    @Test
    @DisplayName("confirmedAt이 null이면 INVALID_LIFECYCLE_META 예외가 발생한다")
    void create_withNullConfirmedAt_throwsException() {
        // given / when / then
        assertThatThrownBy(() -> ItemStatusInfo.create(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("CREATED → VALIDATING 상태 전이에 성공한다")
    void validating_fromCreated_succeeds() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0));

        // when
        ItemStatusInfo result = statusInfo.validating();

        // then
        assertThat(result.getStatus()).isEqualTo(SettlementItemStatus.VALIDATING);
    }

    @Test
    @DisplayName("CREATED가 아닌 상태에서 validating 호출 시 예외가 발생한다")
    void validating_fromNonCreated_throwsException() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0))
                .ready();

        // when / then
        assertThatThrownBy(statusInfo::validating)
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("VALIDATING → READY 상태 전이에 성공한다")
    void ready_fromValidating_succeeds() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0))
                .validating()
                .ready();

        // then
        assertThat(statusInfo.getStatus()).isEqualTo(SettlementItemStatus.READY);
    }

    @Test
    @DisplayName("READY → PROCESSING 상태 전이에 성공한다")
    void processing_fromReady_succeeds() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0))
                .validating()
                .ready()
                .processing();

        // then
        assertThat(statusInfo.getStatus()).isEqualTo(SettlementItemStatus.PROCESSING);
    }

    @Test
    @DisplayName("READY가 아닌 상태에서 processing 호출 시 예외가 발생한다")
    void processing_fromNonReady_throwsException() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0));

        // when / then
        assertThatThrownBy(statusInfo::processing)
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("PROCESSING → COMPLETED 상태 전이에 성공하고 settledAt이 설정된다")
    void complete_fromProcessing_succeeds() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0))
                .validating()
                .ready()
                .processing();

        // when
        ItemStatusInfo result = statusInfo.complete();

        // then
        assertThat(result.getStatus()).isEqualTo(SettlementItemStatus.COMPLETED);
        assertThat(result.getSettledAt()).isNotNull();
    }

    @Test
    @DisplayName("PROCESSING이 아닌 상태에서 complete 호출 시 예외가 발생한다")
    void complete_fromNonProcessing_throwsException() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0))
                .validating()
                .ready();

        // when / then
        assertThatThrownBy(statusInfo::complete)
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("failToValidate 호출 시 VALIDATE_FAILED 상태가 된다")
    void failToValidate_succeeds() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0))
                .validating();

        // when
        ItemStatusInfo result = statusInfo.failToValidate();

        // then
        assertThat(result.getStatus()).isEqualTo(SettlementItemStatus.VALIDATE_FAILED);
    }

    @Test
    @DisplayName("failToExecute 호출 시 FAILED 상태가 된다")
    void failToExecute_succeeds() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0))
                .validating()
                .ready()
                .processing();

        // when
        ItemStatusInfo result = statusInfo.failToExecute();

        // then
        assertThat(result.getStatus()).isEqualTo(SettlementItemStatus.FAILED);
    }

    @Test
    @DisplayName("manual 호출 시 MANUAL 상태가 된다")
    void manual_succeeds() {
        // given
        ItemStatusInfo statusInfo = ItemStatusInfo.create(LocalDateTime.of(2025, 3, 15, 10, 0));

        // when
        ItemStatusInfo result = statusInfo.manual();

        // then
        assertThat(result.getStatus()).isEqualTo(SettlementItemStatus.MANUAL);
    }
}
