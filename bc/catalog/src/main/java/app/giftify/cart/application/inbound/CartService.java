package app.giftify.cart.application.inbound;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import app.giftify.cart.adapter.inbound.CartItemResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.cart.adapter.inbound.CartResponse;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.CartItem;
import app.giftify.cart.core.domain.CartItemAddResult;
import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService
	implements AddCartItemUseCase, CartCreateUseCase, GetCartUseCase, RemoveCartItemUseCase, ClearCartUseCase {
	private final CartRepositoryPort cartRepositoryPort;
	private final ProductRepositoryPort productRepositoryPort;
	private final WishlistItemRepositoryPort wishlistItemRepositoryPort;

	@Override
	public Cart createCart(Long memberId) {
		return cartRepositoryPort.save(Cart.create(memberId));
	}

	@Override
	public CartItemAddResult addItemToMyCart(Long memberId, AddCartItemCommand command) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

		// 타입 검증
		TargetType targetType = command.cartItemKey().targetType();
		validateFundingTarget(targetType);

		// 펀딩 구매 검증
		Long wishlistItemId = command.cartItemKey().targetId();
		validateFundingPurchase(wishlistItemId);

		CartItemAddResult result = cart.addItem(targetType, wishlistItemId, command.amount());
		cartRepositoryPort.save(cart);

		return result;
	}

	@Override
	public void addItemsToMyCart(Long memberId, List<AddCartItemCommand> commands) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
				.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

		for (AddCartItemCommand command : commands) {
			// 타입 검증
			TargetType targetType = command.cartItemKey().targetType();
			validateFundingTarget(targetType);

			// 펀딩 구매 검증
			Long wishlistItemId = command.cartItemKey().targetId();
			validateFundingPurchase(wishlistItemId);

			cart.addItem(targetType, wishlistItemId, command.amount());
		}

		cartRepositoryPort.save(cart);
	}

	private void validateFundingTarget(TargetType targetType) {
		if (targetType != TargetType.FUNDING_PENDING && targetType != TargetType.FUNDING) {
			throw new CartException(CartErrorCode.INVALID_TARGET_TYPE);
		}
	}

	// 펀딩 구매 검증 (Product + WishlistItem 둘 다 검증)
	private void validateFundingPurchase(Long wishlistItemId) {
		WishlistItem wishlistItem = wishlistItemRepositoryPort.findById(wishlistItemId)
			.orElseThrow(() -> new CartException(CartErrorCode.WISHLIST_ITEM_NOT_FOUND));

		// WishlistItem 상태 검증
		WishlistItemStatus status = wishlistItem.getWishlistItemStatus();
		if (status != WishlistItemStatus.PENDING && status != WishlistItemStatus.IN_PROGRESS) {
			throw new CartException(CartErrorCode.INVALID_ITEM_STATUS, wishlistItemId);
		}

		// 원본 Product 상태 검증
		Product product = productRepositoryPort.findById(wishlistItem.getProductId())
			.orElseThrow(() -> new CartException(CartErrorCode.PRODUCT_NOT_FOUND));

		if (product.getStatus() != ProductStatus.ACTIVE || product.getStock() <= 0) {
			throw new CartException(CartErrorCode.INVALID_ITEM_STATUS, wishlistItemId);
		}
	}

	// 카트 조회(아이템 목록)
	@Override
	public CartResponse getCart(Long cartId, Long memberId) {
		Cart cart = cartRepositoryPort.findById(cartId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

		if (!cart.getMemberId().equals(memberId)) {
			throw new CartException(CartErrorCode.FORBIDDEN);
		}

		return getCartResponse(cart);
	}

	// 내 카트 조회(아이템 목록)
	public CartResponse getMyCart(Long memberId) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

		return getCartResponse(cart);
	}

	@NonNull
	private CartResponse getCartResponse(Cart cart) {
		if (cart.getItems().isEmpty()) {
			return CartResponse.from(cart, List.of());
		}

		// 1. 장바구니의 모든 wishlistItemId를 가져옵니다.
		List<Long> wishlistItemIds = cart.getItems().stream()
				.map(CartItem::getTargetId)
				.toList();

		// 2. wishlistItemId로 WishlistItem 목록을 조회하고 Map으로 만듭니다.
		Map<Long, WishlistItem> wishlistItemMap = wishlistItemRepositoryPort.findAllById(wishlistItemIds).stream()
				.collect(Collectors.toMap(WishlistItem::getId, Function.identity()));

		// 3. 유효하지 않은 WishlistItem(펀딩 종료 등)의 id를 분류합니다.
		Set<Long> fundingEndedIds = cart.getItems().stream()
				.map(CartItem::getTargetId)
				.filter(id -> {
					WishlistItem wishlistItem = wishlistItemMap.get(id);
					if (wishlistItem == null) return true;
					WishlistItemStatus status = wishlistItem.getWishlistItemStatus();
					return status != WishlistItemStatus.PENDING && status != WishlistItemStatus.IN_PROGRESS;
				})
				.collect(Collectors.toSet());

		// 4. 유효한 WishlistItem에서 productId를 추출하여 Product 정보를 조회하고 Map으로 만듭니다.
		List<Long> productIds = wishlistItemMap.values().stream()
				.filter(item -> !fundingEndedIds.contains(item.getId()))
				.map(WishlistItem::getProductId)
				.toList();
		Map<Long, Product> productMap = productRepositoryPort.findAllById(productIds).stream()
				.collect(Collectors.toMap(Product::getId, Function.identity()));

		// 5. wishlistItemId를 키로, Product를 값으로 하는 최종 맵을 생성합니다.
		Map<Long, Product> finalProductMap = new HashMap<>();
		for (WishlistItem wishlistItem : wishlistItemMap.values()) {
			if (!fundingEndedIds.contains(wishlistItem.getId())) {
				finalProductMap.put(wishlistItem.getId(), productMap.get(wishlistItem.getProductId()));
			}
		}

		// 6. 서비스에서 CartItemResponse를 직접 조립합니다.
		List<CartItemResponse> itemResponses = cart.getItems().stream()
				.map(item -> CartItemResponse.from(
						item,
						fundingEndedIds.contains(item.getTargetId()),
						finalProductMap.get(item.getTargetId())
				))
				.toList();

		return CartResponse.from(cart, itemResponses);
	}


	// 내 카트에서 상품 제거
	@Override
	public void removeItems(Long memberId, TargetType targetType, List<Long> targetIds) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
		cart.removeItems(targetType, targetIds);
		cartRepositoryPort.save(cart);
	}

	// 내 카트 전체 비우기
	@Override
	public void clearCart(Long memberId) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
		cart.clearItems();
		cartRepositoryPort.save(cart);
	}
}
