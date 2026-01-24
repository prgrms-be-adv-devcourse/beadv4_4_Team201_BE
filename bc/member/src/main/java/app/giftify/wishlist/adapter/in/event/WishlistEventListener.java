package app.giftify.wishlist.adapter.in.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import app.giftify.shared.domain.event.product.ProductReplicaCreationRequestedEvent;
import app.giftify.shared.domain.event.product.ProductReplicaUpdatedEvent;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import app.giftify.shared.domain.event.product.ProductSaleEnabledEvent;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishlistEventListener {

	private final WishlistProductReplicaPort wishlistProductReplicaPort;
	private final WishlistItemRepositoryPort wishlistItemRepositoryPort;

	// todo 트랜잭션 생각해보기

	// 상품의 상태가 [판매 가능]으로 변경되었을 때
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleProductSaleEnabled(ProductSaleEnabledEvent event) {
		log.info("[Wishlist] 상품의 상태가 [판매 가능]으로 변경되었습니다 | productId: {}", event.getProductId());
		// 위시리스트ID로 레플리카 조회
		wishlistProductReplicaPort.findByProductId(event.getProductId())
			.ifPresentOrElse(
				// 존재하면 상품 상태 true로 변경
				replica -> {
					replica.enableSale();
					wishlistProductReplicaPort.upsert(replica);
				},
				// 존재하지 않으면 상품 조회 API 요청 후 값 받아와 레플리카 새롭게 생성
				() -> {
					WishlistProductReplica newReplica = WishlistProductReplica.builder()
						.productId(event.getProductId())
						.wishlistAllowed(true)
						.build();
					wishlistProductReplicaPort.upsert(newReplica);
				}
			);
	}

	// 상품의 상태가 [판매 불가능]으로 변경되었을 때
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleProductSaleDisabled(ProductSaleDisabledEvent event) {
		log.info("[Wishlist] 상품의 상태가 [판매 불가능]으로 변경되었습니다 | productId: {}", event.getProductId());
		wishlistProductReplicaPort.findByProductId(event.getProductId())
			.ifPresentOrElse(
				// 존재하면 상품 상태 false로 변경
				replica -> {
					replica.disableSale();
					wishlistProductReplicaPort.upsert(replica);
				},
				// 존재하지 않으면 상품 조회 API 요청 후 값 받아와 레플리카 새롭게 생성
				() -> {
					WishlistProductReplica newReplica = WishlistProductReplica.builder()
						.productId(event.getProductId())
						.wishlistAllowed(false)
						.build();
					wishlistProductReplicaPort.upsert(newReplica);
				}
			);
	}

	// 상품이 등록되었을 때
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleProductReplicaCreationRequested(ProductReplicaCreationRequestedEvent event) {
		log.info("[Wishlist] 상품이 등록되었습니다 | productId: {}", event.getId());
		wishlistProductReplicaPort.findByProductId(event.getId())
			.ifPresentOrElse(
				// 존재하면 값만 업데이트
				replica -> {
					replica.updateInfo(
						event.getName(),
						event.getPrice(),
						event.getSellerNickname()
					);
					wishlistProductReplicaPort.upsert(replica);
				},
				// 존재하지 않으면 새로운 레플리카 생성
				() -> {
					WishlistProductReplica newReplica = WishlistProductReplica.builder()
						.productId(event.getId())
						.name(event.getName())
						.price(event.getPrice())
						.sellerNickname(event.getSellerNickname())
						.wishlistAllowed(false)
						.build();
					wishlistProductReplicaPort.upsert(newReplica);
				}
			);
	}

	// 상품 정보가 변경되었을 때
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleProductReplicaUpdated(ProductReplicaUpdatedEvent event) {
		log.info("[Wishlist] Product snapshot updated event received for productId: {}", event.getId());
		wishlistProductReplicaPort.findByProductId(event.getId())
			.ifPresentOrElse(
				replica -> {
					replica.updateInfo(event.getName(), event.getPrice(), event.getSellerNickname());
					wishlistProductReplicaPort.upsert(replica);
				},
				() -> {
					WishlistProductReplica newReplica = WishlistProductReplica.builder()
						.productId(event.getId())
						.name(event.getName())
						.price(event.getPrice())
						.sellerNickname(event.getSellerNickname())
						.wishlistAllowed(false)
						.build();
					wishlistProductReplicaPort.upsert(newReplica);
				}
			);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleFundingCreated(FundingCreatedEvent event) {
		updateStatus(event.getWishlistItemId(),
			WishlistItemStatus.IN_PROGRESS,
			"[Wishlist] 위시리스트상품의 펀딩이 시작되었습니다.");
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleFundingAchieved(FundingAchievedEvent event) {
		updateStatus(event.getWishlistItemId(),
			WishlistItemStatus.REQUESTED_CONFIRM,
			"[Wishlist] 위시리스트상품의 펀딩이 달성되었습니다.");
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleFundingAccepted(FundingAcceptedEvent event) {
		updateStatus(event.getWishlistItemId(),
			WishlistItemStatus.COMPLETED,
			"[Wishlist] 위시리스트상품의 펀딩이 수락되었습니다.");
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleFundingCanceled(FundingCanceledEvent event) {
		updateStatus(event.getWishlistItemId(),
			WishlistItemStatus.NO_THANKS,
			"[Wishlist] 위시리스트상품의 펀딩이 거절되었습니다.");
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleFundingExpired(FundingExpiredEvent event) {
		updateStatus(event.getWishlistItemId(),
			WishlistItemStatus.PENDING,
			"[Wishlist] 위시리스트상품의 펀딩이 만료되었습니다.");
	}

	private void updateStatus(Long wishlistItemId, WishlistItemStatus next, String message) {
		WishlistItem wishlistItem = wishlistItemRepositoryPort.findById(wishlistItemId)
			.orElseThrow(WishlistNotFoundException::new);

		log.info("{} | productId: {}", message, wishlistItem.getProductId());
		wishlistItem.changeStatus(next);
	}
}
