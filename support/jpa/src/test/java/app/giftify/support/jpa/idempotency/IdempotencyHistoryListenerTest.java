package app.giftify.support.jpa.idempotency;

import app.giftify.shared.domain.event.IdempotencySuccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IdempotencyHistoryListenerTest {

    @Mock
    private IdempotencyHistoryRepository repository;

    @InjectMocks
    private IdempotencyHistoryListener listener;

    @Test
    @DisplayName("성공: 이벤트가 발행되면 내역을 생성하여 저장한다")
    void should_save_history_when_event_is_handled() {
        // given
        IdempotencySuccessEvent event = new IdempotencySuccessEvent(
                "test-key-123",
                "hash-abc",
                "ORDER",
                100L
        );

        // when
        listener.handleIdempotencySuccess(event);

        // then
        // 1. repository.save()가 정확히 1번 호출되었는지 검증
        ArgumentCaptor<IdempotencyHistory> captor = ArgumentCaptor.forClass(IdempotencyHistory.class);
        verify(repository, times(1)).save(captor.capture());

        // 2. 저장된 엔티티의 값이 이벤트 데이터와 일치하는지 검증
        IdempotencyHistory savedHistory = captor.getValue();
        assertThat(savedHistory.getIdempotencyKey()).isEqualTo(event.getIdempotencyKey());
        assertThat(savedHistory.getPayloadHash()).isEqualTo(event.getPayloadHash());
        assertThat(savedHistory.getDomainType()).isEqualTo(event.getDomainType());
        assertThat(savedHistory.getRequesterId()).isEqualTo(event.getRequesterId());
    }
}