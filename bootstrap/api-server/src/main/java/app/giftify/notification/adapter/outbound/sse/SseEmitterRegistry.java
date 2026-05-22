package app.giftify.notification.adapter.outbound.sse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRegistry {

	private static final Logger log = LoggerFactory.getLogger(SseEmitterRegistry.class);

	private static final long TIMEOUT = 30 * 60 * 1000L;

	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter create(Long memberId) {
		remove(memberId);

		SseEmitter emitter = new SseEmitter(TIMEOUT);

		emitter.onCompletion(() -> {
			log.debug("SSE connection completed: memberId={}", memberId);
			emitters.remove(memberId);
		});
		emitter.onTimeout(() -> {
			log.debug("SSE connection timed out: memberId={}", memberId);
			emitters.remove(memberId);
		});
		emitter.onError(ex -> {
			log.debug("SSE connection error: memberId={}, error={}", memberId, ex.getMessage());
			emitters.remove(memberId);
		});

		emitters.put(memberId, emitter);
		return emitter;
	}

	public SseEmitter get(Long memberId) {
		return emitters.get(memberId);
	}

	public void remove(Long memberId) {
		SseEmitter existing = emitters.remove(memberId);
		if (existing != null) {
			existing.complete();
		}
	}
}
