package app.giftify.shared.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync // NOTE 문제가 있음. api-server 하나로 돌리면 다른 모듈에 동일 설정이 생기는 순간 서로 충돌 남
public class AsyncConfig implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		// 코어 스레드 수 (기본 유지)
		executor.setCorePoolSize(4);

		// 최대 스레드 수
		executor.setMaxPoolSize(16);

		// 큐 용량 (대기열)
		executor.setQueueCapacity(100);

		// 스레드 이름 접두사
		executor.setThreadNamePrefix("event-async-");

		// 종료 시 태스크 완료 대기
		executor.setWaitForTasksToCompleteOnShutdown(true);

		// 최대 대기 시간 (초)
		executor.setAwaitTerminationSeconds(30);

		// 큐가 가득 찼을 때 CallerRuns 정책 (호출 스레드에서 실행)
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		executor.initialize();

		log.info("[AsyncConfig] 비동기 이벤트 처리용 스레드풀 초기화 완료. " +
			"corePoolSize={}, maxPoolSize={}, queueCapacity={}",
			executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}
}
