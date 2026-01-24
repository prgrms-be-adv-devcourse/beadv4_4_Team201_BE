package app.giftify.wishlist.adapter.out.jpa.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistProductReplicaJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistProductReplicaJpaRepository;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;

@ExtendWith(MockitoExtension.class)
class WishlistProductReplicaAdapterTest {

	@Mock
	private WishlistProductReplicaJpaRepository repository;

	private WishlistProductReplicaAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new WishlistProductReplicaAdapter(repository);
	}

	@Nested
	@DisplayName("findByProductId")
	class FindByProductIdTests {

		@Test
		@DisplayName("productId로 레플리카를 조회하면 도메인 객체로 변환하여 반환한다")
		void findByProductId_ExistingReplica_ReturnsDomain() {
			// given
			Long productId = 1L;
			LocalDateTime updatedAt = LocalDateTime.now();

			WishlistProductReplicaJpaEntity entity = WishlistProductReplicaJpaEntity.builder()
				.productId(productId)
				.name("Test Product")
				.price(10000)
				.sellerNickname("TestSeller")
				.wishlistAllowed(true)
				.updatedAt(updatedAt)
				.build();

			given(repository.findByProductId(productId)).willReturn(Optional.of(entity));

			// when
			Optional<WishlistProductReplica> result = adapter.findByProductId(productId);

			// then
			assertThat(result).isPresent();
			WishlistProductReplica replica = result.get();
			assertThat(replica.getProductId()).isEqualTo(productId);
			assertThat(replica.getName()).isEqualTo("Test Product");
			assertThat(replica.getPrice()).isEqualTo(10000);
			assertThat(replica.getSellerNickname()).isEqualTo("TestSeller");
			assertThat(replica.isWishlistAllowed()).isTrue();
			assertThat(replica.getUpdatedAt()).isEqualTo(updatedAt);
		}

		@Test
		@DisplayName("존재하지 않는 productId로 조회하면 빈 Optional을 반환한다")
		void findByProductId_NonExistingReplica_ReturnsEmpty() {
			// given
			Long productId = 999L;
			given(repository.findByProductId(productId)).willReturn(Optional.empty());

			// when
			Optional<WishlistProductReplica> result = adapter.findByProductId(productId);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("upsert")
	class UpsertTests {

		@Test
		@DisplayName("레플리카가 존재하지 않으면 새로 저장한다")
		void upsert_NonExistingReplica_SavesNew() {
			// given
			Long productId = 1L;
			LocalDateTime updatedAt = LocalDateTime.now();

			WishlistProductReplica domain = WishlistProductReplica.builder()
				.productId(productId)
				.name("New Product")
				.price(15000)
				.sellerNickname("NewSeller")
				.wishlistAllowed(true)
				.updatedAt(updatedAt)
				.build();

			given(repository.findByProductId(productId)).willReturn(Optional.empty());

			// when
			adapter.upsert(domain);

			// then
			ArgumentCaptor<WishlistProductReplicaJpaEntity> captor =
				ArgumentCaptor.forClass(WishlistProductReplicaJpaEntity.class);
			verify(repository).save(captor.capture());

			WishlistProductReplicaJpaEntity saved = captor.getValue();
			assertThat(saved.getProductId()).isEqualTo(productId);
			assertThat(saved.getName()).isEqualTo("New Product");
			assertThat(saved.getPrice()).isEqualTo(15000);
			assertThat(saved.getSellerNickname()).isEqualTo("NewSeller");
			assertThat(saved.isWishlistAllowed()).isTrue();
		}

		@Test
		@DisplayName("레플리카가 존재하면 기존 엔티티를 업데이트한다")
		void upsert_ExistingReplica_UpdatesExisting() {
			// given
			Long productId = 1L;
			LocalDateTime oldUpdatedAt = LocalDateTime.now().minusDays(1);
			LocalDateTime newUpdatedAt = LocalDateTime.now();

			WishlistProductReplicaJpaEntity existingEntity = WishlistProductReplicaJpaEntity.builder()
				.productId(productId)
				.name("Old Product")
				.price(10000)
				.sellerNickname("OldSeller")
				.wishlistAllowed(false)
				.updatedAt(oldUpdatedAt)
				.build();

			WishlistProductReplica domain = WishlistProductReplica.builder()
				.productId(productId)
				.name("Updated Product")
				.price(20000)
				.sellerNickname("UpdatedSeller")
				.wishlistAllowed(true)
				.updatedAt(newUpdatedAt)
				.build();

			given(repository.findByProductId(productId)).willReturn(Optional.of(existingEntity));

			// when
			adapter.upsert(domain);

			// then
			verify(repository, never()).save(any());
			assertThat(existingEntity.getName()).isEqualTo("Updated Product");
			assertThat(existingEntity.getPrice()).isEqualTo(20000);
			assertThat(existingEntity.getSellerNickname()).isEqualTo("UpdatedSeller");
			assertThat(existingEntity.isWishlistAllowed()).isTrue();
			assertThat(existingEntity.getUpdatedAt()).isEqualTo(newUpdatedAt);
		}

		@Test
		@DisplayName("판매 상태만 변경할 때도 정상적으로 업데이트된다")
		void upsert_OnlySaleStatusChanged_UpdatesCorrectly() {
			// given
			Long productId = 1L;
			LocalDateTime updatedAt = LocalDateTime.now();

			WishlistProductReplicaJpaEntity existingEntity = WishlistProductReplicaJpaEntity.builder()
				.productId(productId)
				.name("Product")
				.price(10000)
				.sellerNickname("Seller")
				.wishlistAllowed(true)
				.updatedAt(updatedAt.minusHours(1))
				.build();

			WishlistProductReplica domain = WishlistProductReplica.builder()
				.productId(productId)
				.name("Product")
				.price(10000)
				.sellerNickname("Seller")
				.wishlistAllowed(false)
				.updatedAt(updatedAt)
				.build();

			given(repository.findByProductId(productId)).willReturn(Optional.of(existingEntity));

			// when
			adapter.upsert(domain);

			// then
			assertThat(existingEntity.isWishlistAllowed()).isFalse();
			assertThat(existingEntity.getUpdatedAt()).isEqualTo(updatedAt);
		}
	}
}