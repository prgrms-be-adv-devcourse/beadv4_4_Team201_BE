package app.giftify.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import app.giftify.settlement.domain.event.SettlementCreatedEvent;
import app.giftify.support.common.money.Money;

@SpringBootTest(
	classes = EventExternalizationTestApp.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Import(EventExternalizationIntegrationTest.TestListenerConfig.class)
@EmbeddedKafka(
	partitions = 1,
	topics = {"settlement.created"}
)
@TestPropertySource(properties = {
	"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
	"spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
	"spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
})
class EventExternalizationIntegrationTest {

	@TestConfiguration
	static class TestListenerConfig {
		@Bean
		TestSettlementEventListener testSettlementEventListener() {
			return new TestSettlementEventListener();
		}
	}

	static class TestSettlementEventListener {
		private final AtomicReference<SettlementCreatedEvent> received = new AtomicReference<>();

		@ApplicationModuleListener
		void on(SettlementCreatedEvent event) {
			received.set(event);
		}

		SettlementCreatedEvent getReceived() {
			return received.get();
		}
	}

	@Autowired
	ApplicationEventPublisher publisher;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	TransactionTemplate transactionTemplate;

	@Autowired
	TestSettlementEventListener testListener;

	@Test
	@DisplayName("SettlementCreatedEvent 발행 시 Modulith 아웃박스에 기록되고 리스너가 수신한다")
	void settlementCreatedEvent_isRecordedInOutboxAndReceivedByListener() {
		// Given
		SettlementCreatedEvent event = new SettlementCreatedEvent(999L, 1L, Money.of(50000));

		// When
		transactionTemplate.executeWithoutResult(status ->
			publisher.publishEvent(event)
		);

		// Then 1 - @ApplicationModuleListener가 이벤트를 수신
		await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
			assertThat(testListener.getReceived())
				.as("@ApplicationModuleListener가 이벤트를 수신해야 한다")
				.isNotNull()
		);
		assertThat(testListener.getReceived().getSettlementId()).isEqualTo(999L);

		// Then 2 - event_publication 아웃박스에 기록되고 처리 완료
		await().atMost(Duration.ofSeconds(5)).pollDelay(Duration.ofMillis(500)).untilAsserted(() -> {
			List<Map<String, Object>> publications = jdbcTemplate.queryForList(
				"SELECT event_type, listener_id, completion_date FROM event_publication"
			);

			assertThat(publications)
				.as("event_publication 테이블에 레코드가 존재해야 한다")
				.isNotEmpty();

			assertThat(publications)
				.as("SettlementCreatedEvent 타입으로 기록되어야 한다")
				.anyMatch(pub -> pub.get("EVENT_TYPE").toString()
					.contains("SettlementCreatedEvent"));

			assertThat(publications)
				.as("이벤트 처리가 완료되어야 한다 (completion_date != null)")
				.allMatch(pub -> pub.get("COMPLETION_DATE") != null);
		});
	}
}
