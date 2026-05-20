package app.giftify.integration;

import app.giftify.funding.adpater.inbound.FundingScheduler;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.application.FundingFacade;
import app.giftify.funding.application.FundingGetUseCase;
import app.giftify.order.adapter.outbound.batch.ExpirationBatchScheduler;
import app.giftify.order.adapter.outbound.persistence.jpa.JpaOrderItemRepository;
import app.giftify.order.domain.OrderItemStatus;
import app.giftify.product.adapter.inbound.event.ProductEsEventListener;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.service.ProductEsService;
import app.giftify.shared.domain.type.FundingStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FundingConfirmTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FundingConfirmIntegrationTest {

    @Autowired private FundingFacade fundingFacade;
    @Autowired private FundingRepository fundingRepository;
    @Autowired private JpaOrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @MockitoBean FundingScheduler scheduler;
    @MockitoBean FundingGetUseCase getFundingUseCase;
    @MockitoBean ExpirationBatchScheduler expirationBatchScheduler;
    @MockitoBean ProductEsEventListener productEsEventListener;
    @MockitoBean ProductEsService productEsService;

    private final Long fundingId = 1L;
    private final Long productId = 1L;
    private final List<Long> orderItemIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
    private final Long receiverId = 2L;

    @AfterEach
    void cleanUp() throws InterruptedException {
        Thread.sleep(200);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE order_items");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE payment_cancels");
        jdbcTemplate.execute("TRUNCATE TABLE payment_histories");
        jdbcTemplate.execute("TRUNCATE TABLE payments");
        jdbcTemplate.execute("TRUNCATE TABLE wallet_histories");
        jdbcTemplate.execute("TRUNCATE TABLE wallets");
        jdbcTemplate.execute("TRUNCATE TABLE FUNDING_PARTICIPANT_MEMBERS");
        jdbcTemplate.execute("TRUNCATE TABLE fundings");
        jdbcTemplate.execute("TRUNCATE TABLE PRODUCT_STOCK_HISTORIES");
        jdbcTemplate.execute("TRUNCATE TABLE wishlist_items");
        jdbcTemplate.execute("TRUNCATE TABLE wishlists");
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("TRUNCATE TABLE CORE_MEMBER_REPLICAS");
        jdbcTemplate.execute("TRUNCATE TABLE members");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    @DisplayName("수령 확정 요청 시 재고가 차감되고, 주문·펀딩 BC의 상태가 일치한다.")
    @Sql("/sql/funding-confirm-success.sql")
    void 정상_흐름_BC간_정합성_검증() {
        // given
        final int stock = 50;

        // when
        fundingFacade.requestFundingAcceptance(fundingId, receiverId);

        // then
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    // 펀딩 확정 완료
                    FundingStatus status = fundingRepository.findById(1L).orElseThrow().getStatus();
                    assertThat(status).isEqualTo(FundingStatus.ACCEPTED);

                    // 펀딩 주문 확정 완료 (ID 1~8)
                    boolean isAllOrderItemConfirmed = orderItemIds.stream()
                            .allMatch(id -> orderItemRepository.findById(id).orElseThrow().getStatus() == OrderItemStatus.CONFIRMED);
                    assertTrue(isAllOrderItemConfirmed);

                    // 재고 차감 완료
                    assertThat(productRepository.findById(productId).orElseThrow().getStock())
                            .isEqualTo(stock - 1);
                });
    }

    @Test
    @DisplayName("재고 부족으로 차감 실패 시, 주문은 롤백되고 펀딩은 보상 처리된다.")
    @Sql("/sql/funding-confirm-no-stock.sql")
    void 재고_차감_실패시_보상처리_정합성_검증() {
        // given
        final int stock = 0;

        // when
        fundingFacade.requestFundingAcceptance(fundingId, receiverId);

        // then
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // 주문 롤백 — @EventListener 동기 처리라 같은 트랜잭션 롤백
                    boolean isAllRollback = orderItemIds.stream()
                            .allMatch(i -> orderItemRepository.findById(i).orElseThrow().getStatus() == OrderItemStatus.PAID);
                    long countByConfirmed = orderItemIds.stream()
                            .filter(i -> orderItemRepository.findById(i).orElseThrow().getStatus() == OrderItemStatus.CONFIRMED)
                            .count();

                    assertTrue(isAllRollback);
                    assertThat(countByConfirmed).isZero();

                    // 펀딩 보상 처리 — @ApplicationModuleListener 비동기, 관리자 수동 처리
                    assertThat(fundingRepository.findById(fundingId).orElseThrow().getStatus())
                            .isEqualTo(FundingStatus.ACCEPT_FAILED);

                    // 재고 변화 없음
                    assertThat(productRepository.findById(productId).orElseThrow().getStock())
                            .isEqualTo(stock);
                });
    }

    @Test
    @DisplayName("동시에 동일한 확정 요청이 들어와도 중복 처리되지 않는다.")
    @Sql("/sql/funding-confirm-duplicate.sql")
    void 동시_중복_요청_멱등성_보장() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        final int stock = 50;

        // when
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    fundingFacade.requestFundingAcceptance(fundingId, receiverId);
                } catch (Exception ignored) {
                    // 중복 요청 중 하나는 낙관적 락 또는 상태 전이 예외로 실패할 수 있음
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(5, TimeUnit.SECONDS);

        // then
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // 펀딩 확정 완료
                    FundingStatus status = fundingRepository.findById(1L).orElseThrow().getStatus();
                    assertThat(status).isEqualTo(FundingStatus.ACCEPTED);

                    // 펀딩 주문 확정 완료
                    boolean isAllOrderItemConfirmed = orderItemIds.stream()
                            .allMatch(i -> orderItemRepository.findById(i).orElseThrow().getStatus() == OrderItemStatus.CONFIRMED);
                    assertTrue(isAllOrderItemConfirmed);

                    // 재고 차감 완료
                    assertThat(productRepository.findById(productId).orElseThrow().getStock())
                            .isEqualTo(stock - 1);  // 1번만 차감
                });
    }

    @Test
    @DisplayName("이미 확정된 펀딩에 재확정 요청 시 잘못된 상태 전이를 차단한다.")
    @Sql("/sql/funding-confirm-already-done.sql")
    void 이미_확정된_주문_재확정_시도시_차단() {
        // given
        final int stock = 50;

        // when — 이미 ACCEPTED 상태이므로 FundingException(ALREADY_DECIDED) 발생, 상태 변화 없음
        try {
            fundingFacade.requestFundingAcceptance(fundingId, receiverId);
        } catch (Exception ignored) {}

        // then
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // 펀딩 확정 변화 없음
                    FundingStatus status = fundingRepository.findById(1L).orElseThrow().getStatus();
                    assertThat(status).isEqualTo(FundingStatus.ACCEPTED);

                    // 주문 확정 변화 없음
                    boolean isAllOrderItemConfirmed = orderItemIds.stream()
                            .allMatch(i -> orderItemRepository.findById(i).orElseThrow().getStatus() == OrderItemStatus.CONFIRMED);
                    assertTrue(isAllOrderItemConfirmed);

                    // 재고 변화 없음
                    assertThat(productRepository.findById(productId).orElseThrow().getStock())
                            .isEqualTo(stock);
                });
    }

    @Test
    @DisplayName("펀딩 확정은 펀딩 달성 상태에서만 가능하다.")
    @Sql("/sql/funding-confirm-invalid-funding-status.sql")
    void 확정_불가능한_펀딩_상태_확정_시도시_차단() {
        // given
        final int stock = 50;

        // when — IN_PROGRESS 상태이므로 FundingException 발생, 이벤트 미발행, 상태 변화 없음
        try {
            fundingFacade.requestFundingAcceptance(fundingId, receiverId);
        } catch (Exception ignored) {}

        // then
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // 펀딩 상태 변화 없음 (IN_PROGRESS 유지)
                    FundingStatus status = fundingRepository.findById(1L).orElseThrow().getStatus();
                    assertThat(status).isEqualTo(FundingStatus.IN_PROGRESS);


                    // 주문 상태 변화 없음 (SQL에 8개 아이템 ID 1~8 존재, 모두 PAID)
                    boolean isAllExistingOrderItemPaid = orderItemIds.stream()
                            .allMatch(i -> orderItemRepository.findById(i).orElseThrow().getStatus() == OrderItemStatus.PAID);
                    assertTrue(isAllExistingOrderItemPaid);

                    // 재고 변화 없음
                    assertThat(productRepository.findById(productId).orElseThrow().getStock())
                            .isEqualTo(stock);
                });
    }

    @Test
    @DisplayName("펀딩 확정은 펀딩 주문 결제 완료 상태에서만 가능하다.")
    @Sql("/sql/funding-confirm-invalid-order-status.sql")
    void 확정_불가능한_주문_상태_확정_시도시_차단() {
        // given
        final int stock = 50;
        final int savedCanceled = 4;
        final int savePaid = 4;

        // when
        fundingFacade.requestFundingAcceptance(fundingId, receiverId);

        // then
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // 펀딩 확정 실패
                    FundingStatus status = fundingRepository.findById(1L).orElseThrow().getStatus();
                    assertThat(status).isEqualTo(FundingStatus.ACCEPT_FAILED);

                    // 주문 상태 변화 없음
                    long countByCanceled = orderItemIds.stream()
                            .filter(i -> orderItemRepository.findById(i).orElseThrow().getStatus() == OrderItemStatus.CANCELED)
                            .count();
                    long countByPaid = orderItemIds.stream()
                            .filter(i -> orderItemRepository.findById(i).orElseThrow().getStatus() == OrderItemStatus.PAID)
                            .count();

                    assertThat(countByCanceled).isEqualTo(savedCanceled);
                    assertThat(countByPaid).isEqualTo(savePaid);

                    // 재고 변화 없음
                    assertThat(productRepository.findById(productId).orElseThrow().getStock())
                            .isEqualTo(stock);
                });
    }
}