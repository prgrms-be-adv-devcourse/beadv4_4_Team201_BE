package app.giftify.payment.adapter.inbound.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import app.giftify.payment.application.inbound.BulkPaymentAmountUseCase;
import app.giftify.payment.application.inbound.InternalPaymentQueryUseCase;
import app.giftify.payment.application.inbound.InternalPaymentResult;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalPaymentController 테스트")
class InternalPaymentControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Mock
	private InternalPaymentQueryUseCase internalPaymentQueryUseCase;

	@Mock
	private BulkPaymentAmountUseCase bulkPaymentAmountUseCase;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mockMvc = MockMvcBuilders
			.standaloneSetup(new InternalPaymentController(
				internalPaymentQueryUseCase,
				bulkPaymentAmountUseCase
			))
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.build();
	}

	private InternalPaymentResult createTestResult() {
		return new InternalPaymentResult(
			1L, "ORD-001", 100L, PaymentStatus.PAID,
			PaymentType.FUNDING, PaymentMethod.CARD,
			Money.of(10000), Money.of(10000),
			"dec-key", "dec-code"
		);
	}

	@Nested
	@DisplayName("GET /{paymentId}")
	class GetByIdTests {

		@Test
		@DisplayName("존재하는 결제 조회 시 200 OK를 반환한다")
		void getById_Returns200_WhenPaymentExists() throws Exception {
			// given
			given(internalPaymentQueryUseCase.findById(1L)).willReturn(Optional.of(createTestResult()));

			// when & then
			mockMvc.perform(get("/api/internal/payments/{paymentId}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentId").value(1))
				.andExpect(jsonPath("$.orderNumber").value("ORD-001"))
				.andExpect(jsonPath("$.status").value("PAID"));
		}

		@Test
		@DisplayName("존재하지 않는 결제 조회 시 404를 반환한다")
		void getById_Returns404_WhenPaymentNotFound() throws Exception {
			// given
			given(internalPaymentQueryUseCase.findById(999L)).willReturn(Optional.empty());

			// when & then
			mockMvc.perform(get("/api/internal/payments/{paymentId}", 999L))
				.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("GET /by-order-number/{orderNumber}")
	class GetByOrderNumberTests {

		@Test
		@DisplayName("주문번호로 조회 성공 시 200 OK를 반환한다")
		void getByOrderNumber_Returns200_WhenPaymentExists() throws Exception {
			// given
			given(internalPaymentQueryUseCase.findByOrderNumber("ORD-001"))
				.willReturn(Optional.of(createTestResult()));

			// when & then
			mockMvc.perform(get("/api/internal/payments/by-order-number/{orderNumber}", "ORD-001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderNumber").value("ORD-001"))
				.andExpect(jsonPath("$.memberId").value(100));
		}
	}

	@Nested
	@DisplayName("POST /bulk-amounts")
	class BulkAmountsTests {

		@Test
		@DisplayName("정상 요청 시 금액 맵을 반환한다")
		void getBulkAmounts_Returns200_WithAmountMap() throws Exception {
			// given
			Map<Long, Money> amountMap = Map.of(1L, Money.of(10000), 2L, Money.of(20000));
			given(bulkPaymentAmountUseCase.getBulkAmounts(anyList())).willReturn(amountMap);

			// when & then
			mockMvc.perform(post("/api/internal/payments/bulk-amounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(List.of(1L, 2L))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.1").exists())
				.andExpect(jsonPath("$.2").exists());
		}
	}
}
