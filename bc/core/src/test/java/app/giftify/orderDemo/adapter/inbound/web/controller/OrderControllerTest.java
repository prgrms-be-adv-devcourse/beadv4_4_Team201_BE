package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.facade.CoreFacade;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderRequest;
import app.giftify.orderDemo.application.OrderService;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.exception.IdempotencyErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.vo.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import giftify.support.web.idempotency.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private MockMvc mockMvc;

    // 가짜 객체 생성
    private CoreFacade coreFacade = mock(CoreFacade.class);
    private OrderService orderService = mock(OrderService.class);
    private IdempotencyService idempotencyService = mock(IdempotencyService.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String IDEM_HEADER = "X-Idempotency-Key";

    private PlaceOrderItemRequest itemRequest = new PlaceOrderItemRequest(
            100L,                       // wishlistItemId
            200L,                       // receiverId
            Money.of(50000L),          // amount (Money 클래스 구조에 따라 적절히 생성)
            OrderItemType.FUNDING_GIFT          // orderItemType (Enum)
    );

    private PlaceOrderRequest request = new PlaceOrderRequest(
            List.of(itemRequest),       // items
            PaymentMethod.DEPOSIT     // method (Enum)
    );

    private PlaceOrderResult result = new PlaceOrderResult(1L);


    @BeforeEach
    void setUp() {
        // 테스트 대상 컨트롤러 생성
        OrderController orderController = new OrderController(coreFacade, orderService, idempotencyService);

        // MockMvc 설정 (Argument Resolver 주입)
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentMemberId.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return 1L; // @CurrentMemberId에 항상 1L 주입
                    }
                })
                .setControllerAdvice(new OrderExceptionHandler()) // 예외 핸들러 등록
                .build();
    }

    @Test
    @DisplayName("주문 생성 성공 - 멱등성 검증 후 CoreFacade가 호출된다")
    void placeOrder_Success() throws Exception {
        // given
        String key = "test-key-123";

        given(coreFacade.placeOrder(any())).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/v2/orders")
                        .header(IDEM_HEADER, key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 검증: 컨트롤러에서 명시적으로 호출하는 idempotencyService가 실행되었는가?
        verify(idempotencyService, times(1)).validateIdempotency(eq(key), any());
        verify(coreFacade, times(1)).placeOrder(any());
    }

    @Test
    @DisplayName("주문 생성 실패 - 이미 처리된 멱등키인 경우 409 Conflict를 반환한다")
    void placeOrder_Fail_Duplicate() throws Exception {
        // given
        String key = "already-used-key";

        // idempotencyService에서 예외 발생 시나리오
        willThrow(new PolicyException(IdempotencyErrorCode.DUPLICATE_REQUEST))
                .given(idempotencyService).validateIdempotency(eq(key), any());

        // when & then
        mockMvc.perform(post("/api/v2/orders")
                        .header(IDEM_HEADER, key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // Handler가 정상 작동한다면 409
    }
}