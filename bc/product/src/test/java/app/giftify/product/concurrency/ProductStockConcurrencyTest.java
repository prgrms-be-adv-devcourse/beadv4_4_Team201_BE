package app.giftify.product.concurrency;

import app.giftify.product.ProductJpaTestApplication;
import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.service.ProductService;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.domain.event.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductJpaTestApplication.class)
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

    @Autowired
    private MemberRepository memberRepository;

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
                    productService.decreaseStockByOrder(Map.of(productId, 1));
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

    // 재고 이상의 요청이 들어오면 정확하게 예외 전달한다
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
                    productService.decreaseStockByOrder(Map.of(productId, 1));
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

    // 판매자 재고 수정 중 펀딩 차감이 발생하면 CAS 검증으로 stale 덮어쓰기를 방지한다
    @Test
    @DisplayName("CAS 검증 - 판매자가 재고 수정하는 사이에 펀딩 차감이 발생하면 재고 변경 에러를 받는다")
    void sellerStockUpdate_shouldFailWhenStockChangedByFunding() throws InterruptedException {
        // given
        int initialStock = 100;

        Member seller = memberRepository.saveAndFlush(new Member(9991L, "테스트판매자"));

        ProductJpa product = productRepository.saveAndFlush(
                ProductJpa.builder()
                        .sellerId(seller.getId())
                        .name("CAS 테스트 상품")
                        .description("CAS 테스트용")
                        .price(10000)
                        .stock(initialStock)
                        .status(ProductStatus.ACTIVE)
                        .build()
        );

        Long productId = product.getId();

        // 판매자가 조회 시점에 본 재고 = 100
        int sellerExpectedStock = initialStock;

        // when - 펀딩 차감 먼저 실행 (100 → 99)
        productService.decreaseStockByOrder(Map.of(productId, 1));

        // then - 판매자가 재고를 50으로 수정 시도 (expectedStock=100이지만 실제 DB=99)
        ProductUpdateRequestDto updateRequest = new ProductUpdateRequestDto(
                null, null, null, 50, sellerExpectedStock, null, null
        );

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(
                () -> productService.updateProduct(productId, seller.getId(), updateRequest)
        );

        assertThat(thrown)
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("재고가 변경되었습니다");

        // 재고는 펀딩 차감된 99 그대로여야 한다
        ProductJpa result = productRepository.findById(productId).orElseThrow();

        System.out.println("=== CAS 실패 테스트 결과 ===");
        System.out.println("초기 재고: " + initialStock);
        System.out.println("펀딩 차감 후 실제 DB 재고: " + (initialStock - 1));
        System.out.println("판매자가 알고 있던 재고(expectedStock): " + sellerExpectedStock);
        System.out.println("판매자 수정 요청 재고: 50");
        System.out.println("예외 발생: " + thrown.getClass().getSimpleName() + " - " + thrown.getMessage());
        System.out.println("최종 재고: " + result.getStock());

        assertThat(result.getStock())
                .as("CAS 실패로 판매자 수정이 반영되지 않아 펀딩 차감 후 재고(99)가 유지되어야 한다")
                .isEqualTo(initialStock - 1);
    }

    // 5명이 동시에 여러 상품을 주문하면 재고 부족 상품 때문에 일부 주문이 실패한다
    @Test
    @DisplayName("멀티 상품 주문 원자성 검증 - 5명이 동시에 3개 상품을 주문하면 재고가 가장 적은 상품 기준으로 성공/실패가 나뉘고, 실패한 주문은 어떤 상품도 차감하지 않는다")
    void concurrentMultiProductOrder_shouldBeAtomicPerOrder() throws InterruptedException {
        // given
        // 상품A: 재고 3, 상품B: 재고 4, 상품C: 재고 2 (병목)
        ProductJpa productA = productRepository.saveAndFlush(
                ProductJpa.builder()
                        .sellerId(1L).name("상품A").description("테스트용")
                        .price(10000).stock(3).status(ProductStatus.ACTIVE).build()
        );
        ProductJpa productB = productRepository.saveAndFlush(
                ProductJpa.builder()
                        .sellerId(1L).name("상품B").description("테스트용")
                        .price(20000).stock(4).status(ProductStatus.ACTIVE).build()
        );
        ProductJpa productC = productRepository.saveAndFlush(
                ProductJpa.builder()
                        .sellerId(1L).name("상품C").description("테스트용")
                        .price(30000).stock(2).status(ProductStatus.ACTIVE).build()
        );

        Long idA = productA.getId();
        Long idB = productB.getId();
        Long idC = productC.getId();

        int userCount = 5;
        // 각 사용자가 A×1, B×1, C×1 주문
        Map<Long, Integer> orderItems = Map.of(idA, 1, idB, 1, idC, 1);

        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch readyLatch = new CountDownLatch(userCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(userCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger productExceptionCount = new AtomicInteger(0);
        AtomicInteger otherExceptionCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < userCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    productService.decreaseStockByOrder(orderItems);
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
        ProductJpa resultA = productRepository.findById(idA).orElseThrow();
        ProductJpa resultB = productRepository.findById(idB).orElseThrow();
        ProductJpa resultC = productRepository.findById(idC).orElseThrow();

        System.out.println("=== 멀티 상품 주문 동시성 테스트 결과 ===");
        System.out.println("사용자 수: " + userCount);
        System.out.println("주문 성공: " + successCount.get());
        System.out.println("ProductException(재고 부족): " + productExceptionCount.get());
        System.out.println("기타 예외: " + otherExceptionCount.get());
        System.out.println("상품A 재고: 3 → " + resultA.getStock());
        System.out.println("상품B 재고: 4 → " + resultB.getStock());
        System.out.println("상품C 재고: 2 → " + resultC.getStock());

        // 상품C 재고(2)가 병목 → 최대 2명만 성공
        assertThat(successCount.get())
                .as("상품C 재고가 2이므로 최대 2명만 주문에 성공해야 한다")
                .isEqualTo(2);
        assertThat(productExceptionCount.get())
                .as("나머지 3명은 재고 부족으로 실패해야 한다")
                .isEqualTo(3);
        assertThat(otherExceptionCount.get())
                .as("기타 예외는 발생하지 않아야 한다")
                .isEqualTo(0);

        // 성공한 2건만큼만 차감 (실패한 주문은 롤백 → 부분 차감 없음)
        assertThat(resultA.getStock())
                .as("상품A: 성공한 주문 수만큼만 차감되어야 한다 (3 - 2 = 1)")
                .isEqualTo(1);
        assertThat(resultB.getStock())
                .as("상품B: 성공한 주문 수만큼만 차감되어야 한다 (4 - 2 = 2)")
                .isEqualTo(2);
        assertThat(resultC.getStock())
                .as("상품C: 재고가 정확히 0이어야 한다 (2 - 2 = 0)")
                .isEqualTo(0);
    }

    // 판매자가 최신 재고를 확인하고 수정하면 정상 반영된다
    @Test
    @DisplayName("CAS 성공 - 판매자가 최신 재고를 확인하고 수정하면 정상 반영된다")
    void sellerStockUpdate_shouldSucceedWithCorrectExpectedStock() {
        // given
        int initialStock = 100;

        Member seller = memberRepository.saveAndFlush(new Member(9992L, "테스트판매자2"));

        ProductJpa product = productRepository.saveAndFlush(
                ProductJpa.builder()
                        .sellerId(seller.getId())
                        .name("CAS 성공 테스트 상품")
                        .description("CAS 성공 테스트용")
                        .price(10000)
                        .stock(initialStock)
                        .status(ProductStatus.ACTIVE)
                        .build()
        );

        Long productId = product.getId();

        // when - 판매자가 현재 재고(100)를 정확히 알고 50으로 수정
        ProductUpdateRequestDto updateRequest = new ProductUpdateRequestDto(
                null, null, null, 50, initialStock, null, null
        );

        productService.updateProduct(productId, seller.getId(), updateRequest);

        // then
        ProductJpa result = productRepository.findById(productId).orElseThrow();

        System.out.println("=== CAS 성공 테스트 결과 ===");
        System.out.println("초기 재고: " + initialStock);
        System.out.println("판매자가 알고 있던 재고(expectedStock): " + initialStock);
        System.out.println("판매자 수정 요청 재고: 50");
        System.out.println("예외 발생: 없음");
        System.out.println("최종 재고: " + result.getStock());

        assertThat(result.getStock())
                .as("expectedStock과 실제 재고가 일치하므로 수정이 반영되어야 한다")
                .isEqualTo(50);
    }
}
