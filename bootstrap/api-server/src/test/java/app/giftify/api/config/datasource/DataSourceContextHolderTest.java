package app.giftify.api.config.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataSourceContextHolderTest {

	@AfterEach
	void tearDown() {
		DataSourceContextHolder.clear();
	}

	@Test
	@DisplayName("초기 상태에서 get() 은 null 을 반환한다")
	void initialValueIsNull() {
		assertThat(DataSourceContextHolder.get()).isNull();
	}

	@Test
	@DisplayName("set 후 get 은 동일한 값을 반환한다")
	void setThenGet() {
		DataSourceContextHolder.set(DataSourceKey.REPLICA);
		assertThat(DataSourceContextHolder.get()).isEqualTo(DataSourceKey.REPLICA);
	}

	@Test
	@DisplayName("clear 는 ThreadLocal 값을 제거한다")
	void clearRemovesValue() {
		DataSourceContextHolder.set(DataSourceKey.PRIMARY);
		DataSourceContextHolder.clear();
		assertThat(DataSourceContextHolder.get()).isNull();
	}

	@Test
	@DisplayName("ThreadLocal 은 스레드 간 격리된다")
	void threadLocalIsolated() throws Exception {
		DataSourceContextHolder.set(DataSourceKey.PRIMARY);
		AtomicReference<DataSourceKey> otherThreadValue = new AtomicReference<>(DataSourceKey.PRIMARY);
		CountDownLatch latch = new CountDownLatch(1);
		Thread t = new Thread(() -> {
			otherThreadValue.set(DataSourceContextHolder.get());
			latch.countDown();
		});
		t.start();
		latch.await();
		assertThat(otherThreadValue.get()).isNull();
		assertThat(DataSourceContextHolder.get()).isEqualTo(DataSourceKey.PRIMARY);
	}
}
