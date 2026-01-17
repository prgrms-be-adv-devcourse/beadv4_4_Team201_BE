package domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    @DisplayName("펀딩 결제 생성 시 PAID 상태와 GIFTIFY_CASH 수단으로 생성되어야 한다")
    void createPaidForFunding_ShouldCreatePaymentWithPaidStatus() {
    }

    @Test
    @DisplayName("환불이 성공적으로 수행되어야 한다")
    void refund_ShouldSucceed_WhenStatusIsPaid() {
    }

    @Test
    @DisplayName("이미 환불된 결제를 다시 환불하려 하면 예외가 발생해야 한다")
    void refund_ShouldThrowException_WhenAlreadyRefunded() {
    }

    @Test
    @DisplayName("완료된 펀딩 결제는 취소(Cancel)할 수 없다")
    void cancel_ShouldThrowException_WhenStatusIsPaid() {
    }

    @Test
    @DisplayName("펀딩 수령 확정(Settled) 이후에는 환불할 수 없다")
    void refund_ShouldFail_WhenPaymentIsSettled() {
    }

    @Test
    @DisplayName("PAID 상태의 결제는 수령 확정(SETTLED)할 수 있다")
    void settle_ShouldSucceed_WhenStatusIsPaid() {
    }

    @Test
    @DisplayName("PAID 상태의 결제는 다시 완료 처리할 수 없다")
    void markAsPaid_ShouldThrowException_WhenStatusIsPaid() {

    }

    @Test
    @DisplayName("PENDING 상태가 아닌 결제는 실패 처리할 수 없다")
    void markAsFailed_ShouldThrowException_WhenStatusIsNotPending() {

    }

    @Test
    @DisplayName("PAID 상태가 아닌 결제는 수령 확정(SETTLED)할 수 없다")
    void settle_ShouldThrowException_WhenStatusIsNotPaid() {

    }

    @Test
    @DisplayName("빌더로 생성 시 createdAt이 설정되지 않으면 자동으로 현재 시간이 할당되어야 한다")
    void builder_ShouldCreatePayment_WithDefaultCreatedAt() {
    }

    @Test
    @DisplayName("withId는 기존 필드를 유지한 채 ID가 할당된 새로운 객체를 반환해야 한다 (불변성 검증)")
    void withId_ShouldReturnNewInstance_WithSameFieldsAndNewId() {

    }

    @Test
    @DisplayName("빌더로 모든 필드를 명시적으로 설정할 수 있어야 한다")
    void builder_ShouldSetAllFieldsCorrectly() {
    }
}
