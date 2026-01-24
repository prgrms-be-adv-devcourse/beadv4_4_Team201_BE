package settlement;

import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.OrderItemInfo;
import domain.settlement.SettlementItem;
import domain.settlement.SettlementItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import settlement.command.CreatePaymentItemCommand;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementItemServiceTest {

    @InjectMocks
    private SettlementItemService settlementItemService;

    @Mock
    private SettlementItemRepository settlementItemRepository;

    private CreatePaymentItemCommand createCommand(Long orderItemId) {
        OrderItemInfo info = new OrderItemInfo(1L, "ORD-001", orderItemId, 1L, Money.of(10000L), LocalDateTime.now());
        return new CreatePaymentItemCommand(100L, info);
    }

    @Test
    @DisplayName("성공: 새로운 주문 항목에 대해 정산 아이템을 생성하고 저장한다.")
    void createPaymentItem_Success() {
        // given
        CreatePaymentItemCommand command = createCommand(1L);
        given(settlementItemRepository.existsByOrderItemId(1L)).willReturn(false);

        // when
        settlementItemService.createPaymentItem(command);

        // then
        verify(settlementItemRepository, times(1)).saveAndFlush(any(SettlementItem.class));
    }

    @Test
    @DisplayName("멱등성: 이미 존재하는 orderItemId인 경우 저장하지 않고 종료한다.")
    void createPaymentItem_AlreadyExists() {
        // given
        CreatePaymentItemCommand command = createCommand(1L);
        given(settlementItemRepository.existsByOrderItemId(1L)).willReturn(true);

        // when
        settlementItemService.createPaymentItem(command);

        // then
        // exists에서 걸러졌으므로 create나 save는 호출되지 않아야 함
        verify(settlementItemRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("동시성 이슈: DB 제약 조건 위반 예외 발생 시 예외를 던지지 않고 정상 종료한다.")
    void createPaymentItem_ConcurrencyIssue() {
        // given
        CreatePaymentItemCommand command = createCommand(1L);
        given(settlementItemRepository.existsByOrderItemId(1L)).willReturn(false);

        // saveAndFlush 시점에 누군가 먼저 저장해서 유니크 제약조건 위반이 발생한 상황 가정
        willThrow(new DataIntegrityViolationException("Unique constraint violation"))
                .given(settlementItemRepository).saveAndFlush(any(SettlementItem.class));

        // when & then
        // 예외가 외부로 던져지지 않고 정상 종료되어야 함 (catch문 작동 확인)
        settlementItemService.createPaymentItem(command);

        verify(settlementItemRepository, times(1)).saveAndFlush(any());
    }
}