package app.giftify.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.exception.ProductException;

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
	@DisplayName("상품 상태 변경 실패 - INACTIVE 상태가 아닌 상품은 활성화할 수 없다")
	void active_fromDraft_throwsException() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		assertThatThrownBy(product::active)
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 상태 변경 실패 - REJECTED 상태의 상품은 비활성화할 수 없다")
	void inActive_fromRejected_throwsException() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
		product.reject();

		assertThatThrownBy(product::inActive)
			.isInstanceOf(ProductException.class);
	}

	@Test
	@DisplayName("상품 상태 변경 실패 - DRAFT로 상태를 변경할 수 없다")
	void updateStatus_toDraft_throwsException() {
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		assertThatThrownBy(() -> product.updateProductStatus(ProductStatus.DRAFT))
			.isInstanceOf(ProductException.class);
	}
}