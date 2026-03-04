package app.giftify.wishlist.adapter.in.scheduler;

import app.giftify.wishlist.application.port.in.DeleteExpiredWishlistItemUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishlistItemDeleteScheduler {
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
