package app.giftify.wishlist.adapter.in.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.wishlist.application.port.in.DeleteExpiredWishlistItemUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistItemDeleteScheduler {
	private static final Logger log = LoggerFactory.getLogger(WishlistItemDeleteScheduler.class);

    private final DeleteExpiredWishlistItemUseCase deleteExpiredWishlistItemUseCase;

    @Async("wishlistTaskExecutor")
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredCompletedItems() {
        try {
            int deletedCount = deleteExpiredWishlistItemUseCase.deleteExpiredCompletedItems();
            log.info("만료된 COMPLETED 위시리스트아이템 삭제 건수: {}", deletedCount);
        } catch (Exception e) {
            log.error("위시리스트아이템 자동 삭제 실패", e);
        }
    }
}
