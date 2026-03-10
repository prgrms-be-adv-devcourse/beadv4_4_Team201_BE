package app.giftify.product.adapter.inbound.scheduler;

import app.giftify.product.application.port.in.HardDeleteExpiredProductUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDeleteScheduler {
    private final HardDeleteExpiredProductUseCase hardDeleteExpiredProductUseCase;

    @Async("productTaskExecutor")
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredProducts() {
        try {
            int deletedCount = hardDeleteExpiredProductUseCase.hardDeleteExpiredProducts();
            log.info("만료된 삭제 상품 하드 삭제 건수: {}", deletedCount);
        } catch (Exception e) {
            log.error("삭제 상품 하드 삭제 실패", e);
        }
    }
}
