package app.giftify.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.vo.Money;

/**
 * OrderItemSnapshot 단위 테스트.
 */
class OrderItemSnapshotTest {

	// ========== 성공 케이스 ========== //

	@Test
	@DisplayName("유효한 값으로 OrderItemSnapshot 생성 성공")
	void 생성_성공() {
		OrderItemSnapshot snapshot = new OrderItemSnapshot(
			"item-001",
			"테스트 상품",
			Money.of(1000),
			3,
			Money.of(3000),
			100L
		);

		assertThat(snapshot.orderItemId()).isEqualTo("item-001");
		assertThat(snapshot.itemName()).isEqualTo("테스트 상품");
		assertThat(snapshot.unitPrice()).isEqualTo(Money.of(1000));
		assertThat(snapshot.quantity()).isEqualTo(3);
		assertThat(snapshot.subtotal()).isEqualTo(Money.of(3000));
		assertThat(snapshot.sellerId()).isEqualTo(100L);
	}

	// ========== 필수 필드 검증 실패 케이스 ========== //

	@Nested
	@DisplayName("Given 필수 필드 누락 시")
	class Given_필수_필드_누락_시 {

		@Test
		@DisplayName("orderItemId null 시 PaymentException 발생")
		void itemId_null() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot(null, "상품명", Money.of(1000), 1, Money.of(1000), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("itemId는 필수");
		}

		@Test
		@DisplayName("orderItemId 빈 문자열 시 PaymentException 발생")
		void itemId_빈_문자열() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot("  ", "상품명", Money.of(1000), 1, Money.of(1000), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("itemId는 필수");
		}

		@Test
		@DisplayName("itemName null 시 PaymentException 발생")
		void itemName_null() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", null, Money.of(1000), 1, Money.of(1000), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("itemName은 필수");
		}

		@Test
		@DisplayName("unitPrice null 시 PaymentException 발생")
		void unitPrice_null() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", "상품명", null, 1, Money.of(1000), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("unitPrice는 필수");
		}

		@Test
		@DisplayName("subtotal null 시 PaymentException 발생")
		void subtotal_null() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", "상품명", Money.of(1000), 1, null, 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("subtotal은 필수");
		}

		@Test
		@DisplayName("sellerId null 시 PaymentException 발생")
		void sellerId_null() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", "상품명", Money.of(1000), 1, Money.of(1000), null))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("sellerId는 필수");
		}
	}

	// ========== 수량 검증 실패 케이스 ========== //

	@Nested
	@DisplayName("Given 수량이 유효하지 않을 때")
	class Given_수량이_유효하지_않을_때 {

		@Test
		@DisplayName("quantity가 0이면 PaymentException 발생")
		void quantity_0() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", "상품명", Money.of(1000), 0, Money.of(0), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("수량은 1 이상");
		}

		@Test
		@DisplayName("quantity가 음수면 PaymentException 발생")
		void quantity_음수() {
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", "상품명", Money.of(1000), -1, Money.of(1000), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("수량은 1 이상");
		}
	}

	// ========== subtotal 계산 검증 실패 케이스 ========== //

	@Nested
	@DisplayName("Given subtotal이 unitPrice × quantity와 일치하지 않을 때")
	class Given_subtotal_불일치 {

		@Test
		@DisplayName("subtotal이 계산값보다 작으면 PaymentException 발생")
		void subtotal_계산값보다_작음() {
			// unitPrice=1000, quantity=3 → expected=3000, actual=2000
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", "상품명", Money.of(1000), 3, Money.of(2000), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("subtotal")
				.hasMessageContaining("일치하지 않습니다");
		}

		@Test
		@DisplayName("subtotal이 계산값보다 크면 PaymentException 발생")
		void subtotal_계산값보다_큼() {
			// unitPrice=1000, quantity=2 → expected=2000, actual=5000
			assertThatThrownBy(() ->
				new OrderItemSnapshot("item-1", "상품명", Money.of(1000), 2, Money.of(5000), 1L))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("subtotal")
				.hasMessageContaining("일치하지 않습니다");
		}
	}
}
