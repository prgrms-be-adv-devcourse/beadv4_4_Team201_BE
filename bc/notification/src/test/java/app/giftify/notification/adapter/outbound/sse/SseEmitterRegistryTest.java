package app.giftify.notification.adapter.outbound.sse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRegistryTest {

	private SseEmitterRegistry registry;

	private final Long memberId1 = 100L;
	private final Long memberId2 = 200L;

	@BeforeEach
	void setUp() {
		registry = new SseEmitterRegistry();
	}

	@Test
	@DisplayName("create: 신규 emitter 생성 후 get 으로 동일 인스턴스 조회 가능")
	void create_NewEmitter() {
		SseEmitter emitter = registry.create(memberId1);

		assertThat(emitter).isNotNull();
		assertThat(registry.get(memberId1)).isSameAs(emitter);
	}

	@Test
	@DisplayName("create: 동일 memberId 재호출 시 기존 emitter 제거 + 신규 emitter 반환")
	void create_ReplacesExisting() {
		SseEmitter first = registry.create(memberId1);
		SseEmitter second = registry.create(memberId1);

		assertThat(second).isNotSameAs(first);
		assertThat(registry.get(memberId1)).isSameAs(second);
	}

	@Test
	@DisplayName("get: 미등록 memberId 는 null 반환")
	void get_ReturnsNullWhenAbsent() {
		assertThat(registry.get(memberId1)).isNull();
	}

	@Test
	@DisplayName("remove: 등록된 emitter 제거")
	void remove_ExistingEmitter() {
		registry.create(memberId1);

		registry.remove(memberId1);

		assertThat(registry.get(memberId1)).isNull();
	}

	@Test
	@DisplayName("remove: 미등록 memberId 는 안전하게 no-op")
	void remove_NoOpWhenAbsent() {
		registry.remove(memberId1);

		assertThat(registry.get(memberId1)).isNull();
	}

	@Test
	@DisplayName("create: 서로 다른 memberId 는 독립적으로 관리")
	void create_DistinctMembersIndependent() {
		SseEmitter e1 = registry.create(memberId1);
		SseEmitter e2 = registry.create(memberId2);

		assertThat(registry.get(memberId1)).isSameAs(e1);
		assertThat(registry.get(memberId2)).isSameAs(e2);
		assertThat(e1).isNotSameAs(e2);
	}
}
