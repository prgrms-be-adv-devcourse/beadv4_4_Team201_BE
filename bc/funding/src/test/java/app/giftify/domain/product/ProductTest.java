package app.giftify.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import app.giftify.shared.domain.event.product.ProductSaleEnabledEvent;

class ProductTest {

	private FundingMember seller;

	@BeforeEach
	void setUp() {
		seller = new FundingMember(1L, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
	}

	@Test
	@DisplayName("상품 생성 성공 - 상품 생성 시 초기 상태는 DRAFT이다")
	void createProduct_initialStatusIsDraft() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		assertThat(product.getName()).isEqualTo("테스트 상품");
		assertThat(product.getDescription()).isEqualTo("테스트 설명");
		assertThat(product.getPrice()).isEqualTo(10000);
		assertThat(product.getStock()).isEqualTo(100);
		assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
	}

	@Test
	@DisplayName("상품 생성 실패 - 판매자가 없으면 예외가 발생한다")
	void createProduct_withoutSeller_throwsException() {
		assertThatThrownBy(() -> new Product(null, "테스트 상품", "테스트 설명", 10000, 100))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 생성 실패 - 상품명이 null이면 예외가 발생한다")
	void createProduct_withNullName_throwsException() {
		assertThatThrownBy(() -> new Product(seller, null, "테스트 설명", 10000, 100))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 생성 실패 - 상품명이 빈 문자열이면 예외가 발생한다")
	void createProduct_withBlankName_throwsException() {
		assertThatThrownBy(() -> new Product(seller, "  ", "테스트 설명", 10000, 100))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 생성 실패 - 설명이 null이면 예외가 발생한다")
	void createProduct_withNullDescription_throwsException() {
		assertThatThrownBy(() -> new Product(seller, "테스트 상품", null, 10000, 100))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 생성 실패 - 설명이 빈 문자열이면 예외가 발생한다")
	void createProduct_withBlankDescription_throwsException() {
		assertThatThrownBy(() -> new Product(seller, "테스트 상품", "  ", 10000, 100))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 생성 실패 - 가격이 0이면 예외가 발생한다")
	void createProduct_withZeroPrice_throwsException() {
		assertThatThrownBy(() -> new Product(seller, "테스트 상품", "테스트 설명", 0, 100))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 생성 실패 - 가격이 음수이면 예외가 발생한다")
	void createProduct_withNegativePrice_throwsException() {
		assertThatThrownBy(() -> new Product(seller, "테스트 상품", "테스트 설명", -1000, 100))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 생성 실패 - 재고가 음수이면 예외가 발생한다")
	void createProduct_withNegativeStock_throwsException() {
		assertThatThrownBy(() -> new Product(seller, "테스트 상품", "테스트 설명", 10000, -1))
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 등록 승인 성공 - DRAFT 상태의 상품을 승인하면 INACTIVE가 된다")
	void approve_fromDraft_becomesInactive() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		product.approve();

		assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
	}

	@Test
	@DisplayName("상품 등록 거절 성공 - DRAFT 상태의 상품을 거절하면 REJECTED가 된다")
	void reject_fromDraft_becomesRejected() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		product.reject();

		assertThat(product.getStatus()).isEqualTo(ProductStatus.REJECTED);
	}

	@Test
	@DisplayName("상품 등록 거절 실패 - DRAFT 상태가 아닌 상품은 거절할 수 없다")
	void reject_fromInactive_throwsException() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
		product.approve();

		assertThatThrownBy(product::reject)
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 상태 변경 성공 - INACTIVE 상태의 상품을 활성화하면 ACTIVE가 된다")
	void active_fromInactive_becomesActive() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
		product.approve();

		product.active();

		assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
	}

	@Test
	@DisplayName("상품 상태 변경 성공 - ACTIVE 상태의 상품을 비활성화하면 INACTIVE가 된다")
	void inActive_fromActive_becomesInactive() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
		product.approve();
		product.active();

		product.inActive();

		assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
	}

	@Test
	@DisplayName("상품 상태 변경 실패 - DRAFT 상태의 상품은 ACTIVE 상태로 변경할 수 없다")
	void active_fromDraft_throwsException() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		assertThatThrownBy(product::active)
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 상태 변경 실패 - REJECTED 상태의 상품은 INACTIVE 상태로 변경할 수 없다")
	void inActive_fromRejected_throwsException() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
		product.reject();

		assertThatThrownBy(product::inActive)
			.isInstanceOf(ProductException.class);
	}

	@Nested
	@DisplayName("상품 정보 수정")
	class UpdateProduct {

		@Test
		@DisplayName("상품명을 수정할 수 있다")
		void updateName_changesName() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

			product.updateName("변경된 상품명");

			assertThat(product.getName()).isEqualTo("변경된 상품명");
		}

		@Test
		@DisplayName("상품 설명을 수정할 수 있다")
		void updateDescription_changesDescription() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

			product.updateDescription("변경된 설명");

			assertThat(product.getDescription()).isEqualTo("변경된 설명");
		}

		@Test
		@DisplayName("상품 가격을 수정할 수 있다")
		void updatePrice_changesPrice() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

			product.updatePrice(20000);

			assertThat(product.getPrice()).isEqualTo(20000);
		}
	}

	@Nested
	@DisplayName("재고 수정 - updateStock")
	class UpdateStock {

		@Test
		@DisplayName("재고를 수정할 수 있다")
		void updateStock_changesStock() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

			product.updateStock(50);

			assertThat(product.getStock()).isEqualTo(50);
		}

		@Test
		@DisplayName("상태가 ACTIVE인 상품의 재고가 0이 되면 품절 이벤트가 발생한다")
		void updateStock_toZero_publishesSoldOutEvent() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve();
			product.active();

			product.updateStock(0);

			assertThat(product.pullEvents())
				.hasSize(3)
				.last()
				.isInstanceOf(ProductSaleDisabledEvent.class);
		}

		@Test
		@DisplayName("상태가 ACTIVE인 상품의 재고가 0에서 양수가 되면 판매 가능 이벤트가 발생한다")
		void updateStock_fromZeroToPositive_publishesSaleEnabledEvent() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 0);
			product.approve();
			product.active();

			product.updateStock(50);

			assertThat(product.pullEvents())
				.hasSize(3)
				.last()
				.isInstanceOf(ProductSaleEnabledEvent.class);
		}

		@Test
		@DisplayName("재고가 양수에서 양수로 변경되면 판매 가능 이벤트가 발생하지 않는다")
		void updateStock_positiveToPositive_noEvent() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

			product.updateStock(50);

			assertThat(product.pullEvents()).isEmpty();
		}
	}

	@Nested
	@DisplayName("재고 감소 - decreaseStock")
	class DecreaseStock {

		@Test
		@DisplayName("재고를 감소시킬 수 있다")
		void decreaseStock_reducesStock() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

			product.decreaseStock(30);

			assertThat(product.getStock()).isEqualTo(70);
		}

		@Test
		@DisplayName("재고보다 많은 수량을 감소시키면 예외가 발생한다")
		void decreaseStock_moreThanStock_throwsException() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 10);

			assertThatThrownBy(() -> product.decreaseStock(20))
				.isInstanceOf(ProductException.class);
		}

		@Test
		@DisplayName("상태가 ACTIVE인 상품의 재고가 재고 감소로 0이 되면 품절 이벤트가 발생한다")
		void decreaseStock_toZero_publishesSoldOutEvent() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 10);
			product.approve();
			product.active();

			product.decreaseStock(10);

			assertThat(product.getStock()).isEqualTo(0);
			assertThat(product.pullEvents())
				.hasSize(3)
				.last()
				.isInstanceOf(ProductSaleDisabledEvent.class);
		}
	}

	@Nested
	@DisplayName("재고 증가 - increaseStock")
	class IncreaseStock {

		@Test
		@DisplayName("재고를 증가시킬 수 있다")
		void increaseStock_addsStock() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

			product.increaseStock(50);

			assertThat(product.getStock()).isEqualTo(150);
		}

		@Test
		@DisplayName("상태가 ACTIVE인 상품의 재고가 0에서 증가하면 판매 가능 이벤트가 발생한다")
		void increaseStock_fromZero_publishesSaleEnabledEvent() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 0);
			product.approve();
			product.active();

			product.increaseStock(50);

			assertThat(product.getStock()).isEqualTo(50);
			assertThat(product.pullEvents())
				.hasSize(3)
				.last()
				.isInstanceOf(ProductSaleEnabledEvent.class);
		}

		@Test
		@DisplayName("상태가 ACTIVE인 상품의 재고가 양수에서 증가하면 판매 가능 이벤트가 발생하지 않는다")
		void increaseStock_fromPositive_noEvent() {
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve();
			product.active();
			product.pullEvents(); // approve와 active에서 발생한 이벤트 제거

			product.increaseStock(50);

			assertThat(product.pullEvents()).isEmpty();
		}
	}
}