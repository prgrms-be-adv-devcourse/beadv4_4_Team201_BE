package app.giftify.api.config.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

class DataSourceRoutingAspectTest {

	private DataSourceRoutingAspect aspect;

	@BeforeEach
	void setUp() {
		aspect = new DataSourceRoutingAspect();
	}

	@AfterEach
	void tearDown() {
		DataSourceContextHolder.clear();
	}

	@Test
	@DisplayName("readOnly=true 어노테이션이면 REPLICA 가 설정된 채로 proceed 한다")
	void readOnlySetsReplica() throws Throwable {
		Transactional readOnly = Mockito.mock(Transactional.class);
		Mockito.when(readOnly.readOnly()).thenReturn(true);

		var contextDuringProceed = new java.util.concurrent.atomic.AtomicReference<DataSourceKey>();
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(pjp.proceed()).thenAnswer(inv -> {
			contextDuringProceed.set(DataSourceContextHolder.get());
			return null;
		});

		aspect.routeByTransactional(pjp, readOnly);

		assertThat(contextDuringProceed.get()).isEqualTo(DataSourceKey.REPLICA);
		assertThat(DataSourceContextHolder.get()).isNull();
	}

	@Test
	@DisplayName("readOnly=false 어노테이션이면 PRIMARY 가 설정된 채로 proceed 한다")
	void readWriteSetsPrimary() throws Throwable {
		Transactional readWrite = Mockito.mock(Transactional.class);
		Mockito.when(readWrite.readOnly()).thenReturn(false);

		var contextDuringProceed = new java.util.concurrent.atomic.AtomicReference<DataSourceKey>();
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(pjp.proceed()).thenAnswer(inv -> {
			contextDuringProceed.set(DataSourceContextHolder.get());
			return null;
		});

		aspect.routeByTransactional(pjp, readWrite);

		assertThat(contextDuringProceed.get()).isEqualTo(DataSourceKey.PRIMARY);
		assertThat(DataSourceContextHolder.get()).isNull();
	}

	@Test
	@DisplayName("proceed 도중 예외가 발생해도 context 는 항상 clear 된다")
	void contextClearedEvenOnException() throws Throwable {
		Transactional readOnly = Mockito.mock(Transactional.class);
		Mockito.when(readOnly.readOnly()).thenReturn(true);

		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(pjp.proceed()).thenThrow(new IllegalStateException("boom"));

		assertThatThrownBy(() -> aspect.routeByTransactional(pjp, readOnly))
			.isInstanceOf(IllegalStateException.class);

		assertThat(DataSourceContextHolder.get()).isNull();
	}

	@Test
	@DisplayName("중첩 호출에서 안쪽 context 종료 후 바깥 context 가 복원된다")
	void nestedContextRestored() throws Throwable {
		DataSourceContextHolder.set(DataSourceKey.PRIMARY);

		Transactional readOnly = Mockito.mock(Transactional.class);
		Mockito.when(readOnly.readOnly()).thenReturn(true);

		var contextDuringInner = new java.util.concurrent.atomic.AtomicReference<DataSourceKey>();
		ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(pjp.proceed()).thenAnswer(inv -> {
			contextDuringInner.set(DataSourceContextHolder.get());
			return null;
		});

		aspect.routeByTransactional(pjp, readOnly);

		assertThat(contextDuringInner.get()).isEqualTo(DataSourceKey.REPLICA);
		assertThat(DataSourceContextHolder.get()).isEqualTo(DataSourceKey.PRIMARY);
	}
}
