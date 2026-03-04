package app.giftify.cart.application.inbound;

import app.giftify.cart.adapter.inbound.CartItemResponse;
import app.giftify.cart.adapter.inbound.CartResponse;
import app.giftify.cart.application.inbound.usecase.*;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.CartItem;
import app.giftify.cart.core.domain.CartItemAddResult;
import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.domain.port.FundingQueryPort;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingInfo;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application 단계에서 CARS 논리적 분리
 * MSA 전환 등 필요 시 물리적 분리 가능
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService
	implements AddCartItemUseCase, CartCreateUseCase, GetCartUseCase, RemoveCartItemUseCase, ClearCartUseCase {
	private final CartRepositoryPort cartRepositoryPort;
	private final ProductRepositoryPort productRepositoryPort;
	private final WishlistItemRepositoryPort wishlistItemRepositoryPort;
	private final WishlistRepositoryPort wishlistRepositoryPort;
	private final MemberRepository memberRepository;
    private final FundingQueryPort fundingQueryPort;

    @Override
	public Cart createCart(Long memberId) {
		return cartRepositoryPort.findByMemberId(memberId)
			.orElseGet(() -> cartRepositoryPort.save(Cart.create(memberId)));
	}

	@Override
	public CartItemAddResult addItemToMyCart(Long memberId, AddCartItemCommand command) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

		// 타입 검증
		TargetType targetType = command.cartItemKey().targetType();
		validateFundingTarget(targetType);

		// 장바구니 추가 가능 검증
		Long wishlistItemId = command.cartItemKey().targetId();
		validateCartItemAddable(wishlistItemId, command);

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

			// 장바구니 추가 가능 검증
			Long wishlistItemId = command.cartItemKey().targetId();
			validateCartItemAddable(wishlistItemId, command);

			cart.addItem(targetType, wishlistItemId, command.amount());
		}

		cartRepositoryPort.save(cart);
	}

	private void validateFundingTarget(TargetType targetType) {
		if (targetType != TargetType.FUNDING_PENDING && targetType != TargetType.FUNDING) {
			throw new CartException(CartErrorCode.INVALID_TARGET_TYPE);
		}
	}

	// 장바구니 추가 가능 검증 (Product + WishlistItem 둘 다 검증)
	private void validateCartItemAddable(Long wishlistItemId, AddCartItemCommand command) {
		WishlistItem wishlistItem = wishlistItemRepositoryPort.findById(wishlistItemId)
			.orElseThrow(() -> new CartException(CartErrorCode.WISHLIST_ITEM_NOT_FOUND, wishlistItemId));

		// WishlistItem 상태 검증
		WishlistItemStatus status = wishlistItem.getWishlistItemStatus();
		if (status != WishlistItemStatus.PENDING && status != WishlistItemStatus.IN_PROGRESS) {
			throw new CartException(CartErrorCode.INVALID_ITEM_STATUS, wishlistItemId);
		}

		// 원본 Product 상태 검증
		Product product = productRepositoryPort.findById(wishlistItem.getProductId())
			.orElseThrow(() -> new CartException(CartErrorCode.PRODUCT_NOT_FOUND, wishlistItem.getProductId()));

		if (product.getStatus() != ProductStatus.ACTIVE || product.getStock() <= 0) {
			throw new CartException(CartErrorCode.INVALID_ITEM_STATUS, wishlistItem.getProductId());
		}

        // 상품 금액 검증
        if (command.amount().amount().longValue() > product.getPrice()) {
            throw new CartException(CartErrorCode.EXCEED_PRODUCT_PRICE, product.getPrice());
        }

        // 펀딩 잔여액 검증
		FundingInfo fundingInfo = fundingQueryPort.findFundingInfoByWishlistItemId(wishlistItemId)
				.orElse(null);

		if (fundingInfo != null && command.amount().amount().longValue() > fundingInfo.remainingAmount()) {
			throw new CartException(CartErrorCode.EXCEED_REMAINING_AMOUNT, fundingInfo.remainingAmount());
		}
	}

	// 카트 조회(아이템 목록)
	@Override
	public CartResponse getCart(Long cartId, Long memberId) {
		Cart cart = cartRepositoryPort.findById(cartId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND, cartId));

		if (!cart.getMemberId().equals(memberId)) {
			throw new CartException(CartErrorCode.FORBIDDEN, memberId);
		}

		return getCartResponse(cart);
	}

	// 내 카트 조회(아이템 목록)
	public CartResponse getMyCart(Long memberId) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND , memberId));

		return getCartResponse(cart);
	}

	@NonNull
	private CartResponse getCartResponse(Cart cart) {
		if (cart.getItems().isEmpty()) {
			return CartResponse.from(cart, List.of());
		}

		List<Long> wishlistItemIds = cart.getItems().stream()
				.map(CartItem::getTargetId)
				.toList();

		// 1. WishlistItem 조회
		Map<Long, WishlistItem> wishlistItemMap = wishlistItemRepositoryPort.findAllById(wishlistItemIds).stream()
				.collect(Collectors.toMap(WishlistItem::getId, Function.identity()));

		// 2. Wishlist 조회 → receiver 정보
		List<Long> wishlistIds = wishlistItemMap.values().stream()
				.map(WishlistItem::getWishlistId)
				.distinct()
				.toList();
		Map<Long, Wishlist> wishlistMap = wishlistRepositoryPort.findAllById(wishlistIds).stream()
				.collect(Collectors.toMap(Wishlist::getId, Function.identity()));

		// 3. receiverId 목록으로 Member 조회
		Set<Long> receiverIds = wishlistMap.values().stream()
				.map(Wishlist::getMemberId)
				.collect(Collectors.toSet());
        Map<Long, Member> memberMap = memberRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

		// 4. 유효한 WishlistItem만 필터링
		Set<Long> fundingEndedIds = wishlistItemIds.stream()
				.filter(id -> {
					WishlistItem wi = wishlistItemMap.get(id);
					if (wi == null) return true;
					WishlistItemStatus status = wi.getWishlistItemStatus();
					return status != WishlistItemStatus.PENDING && status != WishlistItemStatus.IN_PROGRESS;
				})
				.collect(Collectors.toSet());

        // 5. Product 조회
		List<Long> productIds = wishlistItemMap.values().stream()
				.filter(wi -> !fundingEndedIds.contains(wi.getId()))
				.map(WishlistItem::getProductId)
				.toList();
		Map<Long, Product> productMap = productRepositoryPort.findAllById(productIds).stream()
				.collect(Collectors.toMap(Product::getId, Function.identity()));

        // 6. FundingInfo 조회
        Map<Long, FundingInfo> fundingInfoMap = fundingQueryPort.findFundingInfoByWishlistItemIds(wishlistItemIds);

        // 7. wishlistItemId 기준으로 CartItemResponse 조립에 필요한 컨텍스트 구성
        record CartItemContext(Product product, Long receiverId, String receiverNickname) {}

        Map<Long, CartItemContext> contextMap = new HashMap<>();
        for (WishlistItem wi : wishlistItemMap.values()) {
            Product product = fundingEndedIds.contains(wi.getId())
					? null : productMap.get(wi.getProductId());
            Wishlist wishlist = wishlistMap.get(wi.getWishlistId());
            Long receiverId = wishlist != null ? wishlist.getMemberId() : null;
            String receiverNickname = receiverId != null && memberMap.get(receiverId) != null
					? memberMap.get(receiverId).getNickname() : null;
            contextMap.put(wi.getId(), new CartItemContext(product, receiverId, receiverNickname));
        }


		// 8. CartItemResponse 조립
		List<CartItemResponse> itemResponses = cart.getItems().stream()
				.map(item -> {
					CartItemContext ctx = contextMap.get(item.getTargetId());
                    FundingInfo fundingInfo = fundingInfoMap.get(item.getTargetId());
					return CartItemResponse.from(
							item,
							fundingEndedIds.contains(item.getTargetId()),
							ctx != null ? ctx.product() : null,
							ctx != null ? ctx.receiverId() : null,
							ctx != null ? ctx.receiverNickname() : null,
                            fundingInfo
					);
				})
				.toList();

		return CartResponse.from(cart, itemResponses);
	}


	// 내 카트에서 상품 제거
	@Override
	public void removeItems(Long memberId, TargetType targetType, List<Long> targetIds) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND, memberId));
		cart.removeItems(targetType, targetIds);
		cartRepositoryPort.save(cart);
	}

	// 내 카트 전체 비우기
	@Override
	public void clearCart(Long memberId) {
		Cart cart = cartRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND, memberId));
		cart.clearItems();
		cartRepositoryPort.save(cart);
	}
}
