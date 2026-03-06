package app.giftify.order.adapter.outbound.batch;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpirationJobListenerTest {

    private ExpirationJobListener listener;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        listener = new ExpirationJobListener();
        logger = (Logger) LoggerFactory.getLogger(ExpirationJobListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    // ===== beforeJob =====

    @Test
    @DisplayName("beforeJob: 배치 시작 로그에 Job 이름이 포함된다")
    void beforeJob_logsJobName() {
        // given
        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn("expirationJob");

        // when
        listener.beforeJob(jobExecution);

        // then
        String log = singleLog();
        assertThat(log)
                .contains("[배치 시작]")
                .contains("expirationJob");
    }

    // ===== afterJob - 성공 =====

    @Test
    @DisplayName("afterJob: 성공 시 실행 상태·소요 시간·처리 건수가 로그에 출력된다")
    void afterJob_success_logsStatistics() {
        // given
        JobExecution jobExecution = successJobExecution(3L, 3L);

        // when
        listener.afterJob(jobExecution);

        // then
        String allLogs = allLogs();
        assertThat(allLogs)
                .contains("[배치 종료]")
                .contains("COMPLETED")
                .contains("읽은 주문 건수: 3")
                .contains("만료 처리 건수: 3");
    }

    @Test
    @DisplayName("afterJob: 여러 Step의 read/write 건수가 합산되어 출력된다")
    void afterJob_aggregatesMultipleStepCounts() {
        // given
        JobExecution jobExecution = mock(JobExecution.class);

        StepExecution step1 = stepExecution(5L, 4L);
        StepExecution step2 = stepExecution(3L, 3L);

        when(jobExecution.getStartTime()).thenReturn(LocalDateTime.now());
        when(jobExecution.getEndTime()).thenReturn(LocalDateTime.now().plusSeconds(2));
        when(jobExecution.getStepExecutions()).thenReturn(List.of(step1, step2));
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);

        // when
        listener.afterJob(jobExecution);

        // then
        String allLogs = allLogs();
        assertThat(allLogs)
                .contains("읽은 주문 건수: 8")
                .contains("만료 처리 건수: 7");
    }

    @Test
    @DisplayName("afterJob: 처리 대상 주문이 없으면 건수가 0으로 출력된다")
    void afterJob_zeroCount_logsZero() {
        // given
        JobExecution jobExecution = successJobExecution(0L, 0L);

        // when
        listener.afterJob(jobExecution);

        // then
        String allLogs = allLogs();
        assertThat(allLogs)
                .contains("읽은 주문 건수: 0")
                .contains("만료 처리 건수: 0");
    }

    // ===== afterJob - 실패 =====

    @Test
    @DisplayName("afterJob: 실패 시 에러 로그와 실패 원인이 출력된다")
    void afterJob_failed_logsErrorMessage() {
        // given
        JobExecution jobExecution = mock(JobExecution.class);
        StepExecution stepExecution = stepExecution(0L, 0L);

        when(jobExecution.getStartTime()).thenReturn(LocalDateTime.now());
        when(jobExecution.getEndTime()).thenReturn(LocalDateTime.now().plusSeconds(1));
        when(jobExecution.getStepExecutions()).thenReturn(List.of(stepExecution));
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);
        when(jobExecution.getAllFailureExceptions())
                .thenReturn(List.of(new RuntimeException("만료 처리 중 오류 발생")));

        // when
        listener.afterJob(jobExecution);

        // then
        String allLogs = allLogs();
        assertThat(allLogs)
                .contains("FAILED")
                .contains("[배치 실패]")
                .contains("만료 처리 중 오류 발생");
    }

    // ===== helpers =====

    private JobExecution successJobExecution(long readCount, long writeCount) {
        JobExecution jobExecution = mock(JobExecution.class);
        StepExecution stepExecution = stepExecution(readCount, writeCount);

        when(jobExecution.getStartTime()).thenReturn(LocalDateTime.now());
        when(jobExecution.getEndTime()).thenReturn(LocalDateTime.now().plusSeconds(1));
        when(jobExecution.getStepExecutions()).thenReturn(List.of(stepExecution));
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);

        return jobExecution;
    }

    private StepExecution stepExecution(long readCount, long writeCount) {
        StepExecution stepExecution = mock(StepExecution.class);
        when(stepExecution.getReadCount()).thenReturn(readCount);
        when(stepExecution.getWriteCount()).thenReturn(writeCount);
        return stepExecution;
    }

    private String singleLog() {
        return listAppender.list.getFirst().getFormattedMessage();
    }

    private String allLogs() {
        return listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }
}
