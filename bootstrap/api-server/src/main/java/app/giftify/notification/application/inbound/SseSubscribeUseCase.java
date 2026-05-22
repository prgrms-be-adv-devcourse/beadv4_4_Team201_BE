package app.giftify.notification.application.inbound;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseSubscribeUseCase {
	SseEmitter subscribe(Long memberId);
}
