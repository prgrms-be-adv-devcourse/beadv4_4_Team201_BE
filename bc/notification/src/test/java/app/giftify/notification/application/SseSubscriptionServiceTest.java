package app.giftify.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.giftify.notification.adapter.outbound.sse.SseEmitterRegistry;

@ExtendWith(MockitoExtension.class)
class SseSubscriptionServiceTest {

	@Mock
	SseEmitterRegistry registry;

	@InjectMocks
	SseSubscriptionService sseSubscriptionService;

	@Nested
	@DisplayName("subscribe")
	class Subscribe {

		@Test
		@DisplayName("registry에서 emitter를 생성하고 반환한다")
		void createsAndReturnsEmitter() {
			Long memberId = 100L;
			SseEmitter emitter = new SseEmitter();

			given(registry.create(memberId)).willReturn(emitter);

			SseEmitter result = sseSubscriptionService.subscribe(memberId);

			assertThat(result).isSameAs(emitter);
			then(registry).should().create(memberId);
		}

		@Test
		@DisplayName("초기 connect 이벤트 전송 실패 시 registry에서 emitter를 제거한다")
		void removesEmitterOnConnectFailure() {
			Long memberId = 100L;
			SseEmitter emitter = new SseEmitter();
			emitter.complete();

			given(registry.create(memberId)).willReturn(emitter);

			sseSubscriptionService.subscribe(memberId);

			then(registry).should().remove(memberId);
		}
	}
}
