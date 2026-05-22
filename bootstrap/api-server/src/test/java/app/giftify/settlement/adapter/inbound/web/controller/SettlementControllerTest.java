package app.giftify.settlement.adapter.inbound.web.controller;

import app.giftify.settlement.application.service.SettlementItemService;
import app.giftify.settlement.application.service.dto.SettlementSummary;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementController 테스트")
class SettlementControllerTest {

    @Mock
    private SettlementItemService settlementItemService;

    @InjectMocks
    private SettlementController settlementController;

    private static final Long SELLER_ID = 1L;

    @Nested
    @DisplayName("getSettlementSummary()")
    class GetSettlementSummaryTests {

        @Test
        @DisplayName("정산 내역을 페이지로 조회해 PageResponse로 래핑해 반환한다")
        void given_sellerId_when_getSettlementSummary_then_returnPageResponse() {
            // given
            SettlementSummary summary = new SettlementSummary(
                    "2024-03", 10L, 10000L, 9700L, 2L, "CREATED"
            );
            Pageable pageable = PageRequest.of(0, 10);
            Page<SettlementSummary> page = new PageImpl<>(List.of(summary), pageable, 1L);

            when(settlementItemService.summarizeSettlements(SELLER_ID, pageable)).thenReturn(page);

            // when
            ResponseEntity<RsData<PageResponse<SettlementSummary>>> response =
                    settlementController.getSettlementSummary(SELLER_ID, pageable);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getResult()).isEqualTo(RsData.Result.SUCCESS);

            PageResponse<SettlementSummary> pageResponse = response.getBody().getData();
            assertThat(pageResponse.content()).hasSize(1);

            SettlementSummary result = pageResponse.content().get(0);
            assertThat(result.settlementMonth()).isEqualTo("2024-03");
            assertThat(result.orderId()).isEqualTo(10L);
            assertThat(result.totalSalesAmount()).isEqualTo(10000L);
            assertThat(result.totalSettlementAmount()).isEqualTo(9700L);
            assertThat(result.itemCount()).isEqualTo(2L);
            assertThat(result.status()).isEqualTo("CREATED");

            assertThat(pageResponse.totalElements()).isEqualTo(1L);
            assertThat(pageResponse.pageNumber()).isEqualTo(0);
            assertThat(pageResponse.pageSize()).isEqualTo(10);
            assertThat(pageResponse.isFirst()).isTrue();

            verify(settlementItemService).summarizeSettlements(SELLER_ID, pageable);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 PageResponse를 반환한다")
        void given_noResults_when_getSettlementSummary_then_returnEmptyPageResponse() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<SettlementSummary> page = new PageImpl<>(List.of(), pageable, 0L);

            when(settlementItemService.summarizeSettlements(SELLER_ID, pageable)).thenReturn(page);

            // when
            ResponseEntity<RsData<PageResponse<SettlementSummary>>> response =
                    settlementController.getSettlementSummary(SELLER_ID, pageable);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            PageResponse<SettlementSummary> pageResponse = response.getBody().getData();
            assertThat(pageResponse.content()).isEmpty();
            assertThat(pageResponse.totalElements()).isEqualTo(0L);
            assertThat(pageResponse.isFirst()).isTrue();
            assertThat(pageResponse.isLast()).isTrue();
        }
    }
}