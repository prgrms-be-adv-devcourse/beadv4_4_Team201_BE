package app.giftify.wishlist.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;

@ExtendWith(MockitoExtension.class)
class WishlistItemServiceTest {

	@Mock
	private WishlistItemRepositoryPort wishlistItemRepositoryPort;

	@Mock
	private WishlistProductReplicaPort wishlistProductReplicaPort;

	@Mock
	private WishlistProductQueryPort wishlistProductQueryPort;

	@Mock
	private WishlistRepositoryPort wishlistRepositoryPort;

	@InjectMocks
	private WishlistItemService wishlistItemService;

	private static final Long MEMBER_ID = 1L;
	private static final Long WISHLIST_ID = 100L;

	@Test
	@DisplayName("위시리스트 아이템을 추가한다")
	void addWishlistItem() {
		// given
		Long productId = 1L;
		AddWishlistItemUseCase.WishlistItemAddCommand command =
			new AddWishlistItemUseCase.WishlistItemAddCommand(productId, WishlistItemStatus.PENDING);

		Wishlist wishlist = Wishlist.builder()
			.id(WISHLIST_ID)
			.memberId(MEMBER_ID)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));
		given(wishlistItemRepositoryPort.findByWishlistIdAndProductId(WISHLIST_ID, productId)).willReturn(
			Optional.empty());

		// Replica가 존재하고 판매 가능함을 가정
		WishlistProductReplica replica = WishlistProductReplica.builder()
			.productId(productId)
			.wishlistAllowed(true)
			.updatedAt(java.time.LocalDateTime.now())
			.build();
		given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.of(replica));

		given(wishlistItemRepositoryPort.save(any(WishlistItem.class))).willAnswer(
			invocation -> invocation.getArgument(0));

		// when
		WishlistItem result = wishlistItemService.addWishlistItem(MEMBER_ID, command);

		// then
		assertThat(result.getWishlistId()).isEqualTo(WISHLIST_ID);
		assertThat(result.getProductId()).isEqualTo(productId);
		verify(wishlistItemRepositoryPort).save(any(WishlistItem.class));
	}

	@Test
	@DisplayName("판매 중이지 않은 상품을 추가하려 하면 예외가 발생한다")
	void addWishlistItemFailStatus() {
		// given
		Long productId = 1L;
		AddWishlistItemUseCase.WishlistItemAddCommand command =
			new AddWishlistItemUseCase.WishlistItemAddCommand(productId, WishlistItemStatus.PENDING);

		Wishlist wishlist = Wishlist.builder()
			.id(WISHLIST_ID)
			.memberId(MEMBER_ID)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));
		given(wishlistItemRepositoryPort.findByWishlistIdAndProductId(WISHLIST_ID, productId)).willReturn(
			Optional.empty());

		// Replica가 없어서 Fallback 발생 상황 가정
		given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.empty());

		// QueryPort에서 판매 중이지 않은 상태 반환
		given(wishlistProductQueryPort.getProductStatus(eq(productId), any()))
			.willReturn(new WishlistProductQueryPort.ProductStatus(productId, false, "Product", 1000, "Seller"));

		// when & then
		assertThatThrownBy(() -> wishlistItemService.addWishlistItem(MEMBER_ID, command))
			.isInstanceOf(app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException.class);
	}

	@Test
	@DisplayName("위시리스트 아이템을 제거한다")
	void removeWishlistItem() {
		// given
		Long productId = 1L;
		RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
			new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(MEMBER_ID, productId);

		Wishlist wishlist = Wishlist.builder()
			.id(WISHLIST_ID)
			.memberId(MEMBER_ID)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));

		WishlistItem wishlistItem = WishlistItem.builder()
			.wishlistId(WISHLIST_ID)
			.productId(productId)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();
		given(wishlistItemRepositoryPort.findByWishlistIdAndProductId(WISHLIST_ID, productId)).willReturn(
			Optional.of(wishlistItem));

		// when
		wishlistItemService.removeWishlistItem(command);

		// then
		verify(wishlistItemRepositoryPort).delete(wishlistItem);
	}

	@Test
	@DisplayName("존재하지 않는 아이템을 제거하려 하면 예외가 발생한다")
	void removeWishlistItemFail() {
		// given
		Long productId = 1L;
		RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
			new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(MEMBER_ID, productId);

		Wishlist wishlist = Wishlist.builder()
			.id(WISHLIST_ID)
			.memberId(MEMBER_ID)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));
		given(wishlistItemRepositoryPort.findByWishlistIdAndProductId(WISHLIST_ID, productId)).willReturn(
			Optional.empty());

		// when & then
		assertThatThrownBy(() -> wishlistItemService.removeWishlistItem(command))
			.isInstanceOf(WishlistNotFoundException.class);
	}

	@Test
	@DisplayName("위시리스트 아이템 목록을 조회한다")
	void getWishlistItems() {
		// given
		Wishlist wishlist = Wishlist.builder()
			.id(WISHLIST_ID)
			.memberId(MEMBER_ID)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));

		List<WishlistItem> items = List.of(
			WishlistItem.builder()
				.wishlistId(WISHLIST_ID)
				.productId(1L)
				.wishlistItemStatus(WishlistItemStatus.PENDING)
				.build(),
			WishlistItem.builder()
				.wishlistId(WISHLIST_ID)
				.productId(2L)
				.wishlistItemStatus(WishlistItemStatus.PENDING)
				.build()
		);
		given(wishlistItemRepositoryPort.findByWishlistId(WISHLIST_ID)).willReturn(items);

		// when
		List<WishlistItem> result = wishlistItemService.getWishlistItems(MEMBER_ID);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(WishlistItem::getProductId).containsExactly(1L, 2L);
	}
}
