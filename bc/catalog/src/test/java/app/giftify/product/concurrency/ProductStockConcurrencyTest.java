package app.giftify.product.concurrency;

import app.giftify.product.ProductTestApplication;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.service.ProductService;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.event.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductTestApplication.class)
@Import(ProductStockConcurrencyTest.TestConfig.class)
@ActiveProfiles("test")
class ProductStockConcurrencyTest {
    // 스레드 1000개도 성공함 (빌드 시간 단축을 위해 100개로 다운)

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EventPublisher eventPublisher() {
            return event -> {
            };
        }
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    // 동시 차감 시 Lost Update가 발생하지 않는다
    @Test
    @DisplayName("재고 차감 정합성 검증 - 동시에 100명이 같은 상품의 재고(100개)를 차감하면 재고가 정확히 0이 되어야 한다")
    void concurrentStockDecrease_shouldNotLoseUpdates() throws InterruptedException {
        // given
        int initialStock = 100;
        int threadCount = 100;

        ProductJpa product = productRepository.saveAndFlush(
                ProductJpa.builder()
                        .sellerId(1L)
                        .name("동시성 테스트 상품")
                        .description("동시성 테스트용")
                        .price(10000)
                        .stock(initialStock)
                        .status(ProductStatus.ACTIVE)
                        .build()
        );

        Long productId = product.getId();

        // 스레드 풀 크기 = 스레드 수 → 모든 스레드가 동시에 실행 가능
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount); // 모든 스레드 준비 대기
        CountDownLatch startLatch = new CountDownLatch(1);           // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(threadCount);  // 완료 대기
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) { // 각 스레드 대기시킴
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();      // 스레드 시작 대기
                    productService.decreaseStockByFunding(productId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();     // 100개 스레드 모두 준비됐는지 확인
        startLatch.countDown(); // 동시 시작
        doneLatch.await();      // 모두 끝날 때까지 대기
        executorService.shutdown();

        // then
        ProductJpa result = productRepository.findById(productId).orElseThrow();
        int expectedStock = initialStock - successCount.get();

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + threadCount);
        System.out.println("초기 재고: " + initialStock);
        System.out.println("성공한 차감 수: " + successCount.get());
        System.out.println("실패한 차감 수: " + failCount.get());
        System.out.println("기대 재고: " + expectedStock);
        System.out.println("실제 재고: " + result.getStock());
        System.out.println("유실된 업데이트 수: " + (result.getStock() - expectedStock));

        assertThat(result.getStock())
                .as("동시성 제어가 없으면 Lost Update로 인해 재고가 정확히 차감되지 않는다")
                .isEqualTo(0);
    }

    // 재고 이상의 요청이 들어오면 정확하게 예외 전달한
    @Test
    @DisplayName("재고 초과 차감 방지 검증 - 동시에 100명이 재고 10개인 상품을 차감하면 10명만 성공하고 90명은 재고 부족 에러를 받는다")
    void concurrentStockDecrease_shouldRejectWhenOutOfStock() throws InterruptedException {
        // given
        int initialStock = 10;
        int threadCount = 100;

        ProductJpa product = productRepository.saveAndFlush(
                ProductJpa.builder()
                        .sellerId(1L)
                        .name("재고 부족 테스트 상품")
                        .description("재고 부족 테스트용")
                        .price(10000)
                        .stock(initialStock)
                        .status(ProductStatus.ACTIVE)
                        .build()
        );

        Long productId = product.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger productExceptionCount = new AtomicInteger(0);
        AtomicInteger otherExceptionCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    productService.decreaseStockByFunding(productId);
                    successCount.incrementAndGet();
                } catch (ProductException e) {
                    productExceptionCount.incrementAndGet();
                } catch (Exception e) {
                    otherExceptionCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        ProductJpa result = productRepository.findById(productId).orElseThrow();

        System.out.println("=== 재고 부족 동시성 테스트 결과 ===");
        System.out.println("스레드 수: " + threadCount);
        System.out.println("초기 재고: " + initialStock);
        System.out.println("성공한 차감 수: " + successCount.get());
        System.out.println("ProductException 수: " + productExceptionCount.get());
        System.out.println("기타 예외 수: " + otherExceptionCount.get());
        System.out.println("실제 재고: " + result.getStock());

        assertThat(successCount.get())
                .as("재고 10개에 대해 정확히 10명만 차감에 성공해야 한다")
                .isEqualTo(initialStock);
        assertThat(productExceptionCount.get())
                .as("나머지 90명은 ProductException(재고 부족)으로 실패해야 한다")
                .isEqualTo(threadCount - initialStock);
        assertThat(otherExceptionCount.get())
                .as("기타 예외는 발생하지 않아야 한다")
                .isEqualTo(0);
        assertThat(result.getStock())
                .as("재고가 정확히 0이어야 한다")
                .isEqualTo(0);
    }
}
