package app.giftify.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.domain.FundingMember;

class ProductSnapshotTest {

	@Nested
	@DisplayName("from() 메서드")
	class FromMethodTests {

		@Test
		@DisplayName("ACTIVE 상태이고 재고가 있는 상품의 스냅샷을 생성한다")
		void from_ActiveProductWithStock_CreatesSnapshotWithOnSaleTrue() {
			// given
			FundingMember seller = new FundingMember(1L, "auth0|123", "판매자닉네임");
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve();
			product.active();

			// when
			ProductSnapshot snapshot = ProductSnapshot.from(product);

			// then
			assertThat(snapshot.getOriginalProductId()).isEqualTo(product.getId());
			assertThat(snapshot.getSellerId()).isEqualTo(seller.getId());
			assertThat(snapshot.getSellerNickname()).isEqualTo("판매자닉네임");
			assertThat(snapshot.getName()).isEqualTo("테스트 상품");
			assertThat(snapshot.getDescription()).isEqualTo("테스트 설명");
			assertThat(snapshot.getPrice()).isEqualTo(10000);
			assertThat(snapshot.isOnSale()).isTrue();
		}

		@Test
		@DisplayName("ACTIVE 상태이지만 재고가 없는 상품의 스냅샷은 onSale이 false이다")
		void from_ActiveProductNoStock_CreatesSnapshotWithOnSaleFalse() {
			// given
			FundingMember seller = new FundingMember(1L, "auth0|123", "판매자닉네임");
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 0);
			product.approve();
			product.active();

			// when
			ProductSnapshot snapshot = ProductSnapshot.from(product);

			// then
			assertThat(snapshot.isOnSale()).isFalse();
		}

		@Test
		@DisplayName("INACTIVE 상태의 상품 스냅샷은 onSale이 false이다")
		void from_InactiveProduct_CreatesSnapshotWithOnSaleFalse() {
			// given
			FundingMember seller = new FundingMember(1L, "auth0|123", "판매자닉네임");
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve(); // INACTIVE 상태

			// when
			ProductSnapshot snapshot = ProductSnapshot.from(product);

			// then
			assertThat(snapshot.isOnSale()).isFalse();
		}
	}

	@Nested
	@DisplayName("Builder")
	class BuilderTests {

		@Test
		@DisplayName("Builder로 스냅샷을 생성한다")
		void builder_CreatesSnapshot() {
			// when
			ProductSnapshot snapshot = ProductSnapshot.builder()
				.originalProductId(1L)
				.sellerId(10L)
				.sellerNickname("판매자")
				.name("상품명")
				.description("설명")
				.price(5000)
				.onSale(true)
				.build();

			// then
			assertThat(snapshot.getOriginalProductId()).isEqualTo(1L);
			assertThat(snapshot.getSellerId()).isEqualTo(10L);
			assertThat(snapshot.getSellerNickname()).isEqualTo("판매자");
			assertThat(snapshot.getName()).isEqualTo("상품명");
			assertThat(snapshot.getDescription()).isEqualTo("설명");
			assertThat(snapshot.getPrice()).isEqualTo(5000);
			assertThat(snapshot.isOnSale()).isTrue();
		}
	}
}