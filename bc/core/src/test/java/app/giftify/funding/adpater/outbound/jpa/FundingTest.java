package app.giftify.funding.adpater.outbound.jpa;

import app.giftify.funding.domain.FundingStatus;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FundingTest {

    @Test
    @DisplayName("expire 성공: 진행 중이고 만료 시간이 지난 경우")
    void expire_Success() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg",1L, 50000);
        // 만료 시간을 과거로 설정
        ReflectionTestUtils.setField(funding, "deadline", LocalDateTime.now().minusDays(1));

        // when
        boolean result = funding.expire(LocalDateTime.now());

        // then
        assertThat(result).isTrue();
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.EXPIRED);
        assertThat(funding.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("expire 멱등성: 이미 종료된 펀딩은 false 반환")
    void expire_Idempotency_AlreadyClosed() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg", 1L, 50000);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.CLOSED);

        // when
        boolean result = funding.expire(LocalDateTime.now());

        // then
        assertThat(result).isFalse();
        // 상태가 변경되지 않아야 함
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.CLOSED);
    }

    @Test
    @DisplayName("expire 멱등성: 이미 만료된 펀딩은 false 반환")
    void expire_Idempotency_AlreadyExpired() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg",1L, 50000);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.EXPIRED);

        // when
        boolean result = funding.expire(LocalDateTime.now());

        // then
        assertThat(result).isFalse();
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.EXPIRED);
    }

    @Test
    @DisplayName("expire 실패: 만료 시간이 지나지 않음")
    void expire_Fail_NotExpiredYet() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg",1L, 50000);
        // 만료 시간이 미래인 상태 (기본값)

        // when & then
        assertThatThrownBy(() -> funding.expire(LocalDateTime.now()))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.IS_NOT_EXPIRED);
    }

    @Test
    @DisplayName("close 성공: 진행 중인 펀딩 종료")
    void close_Success_InProgress() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg",1L, 50000);

        // when
        funding.close();

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.CLOSED);
        assertThat(funding.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("close 성공: 달성된 펀딩 종료")
    void close_Success_Achieved() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg",1L, 50000);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.ACHIEVED);

        // when
        funding.close();

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.CLOSED);
        assertThat(funding.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("close 멱등성: 이미 종료된 펀딩은 상태 변경 없음")
    void close_Idempotency_AlreadyClosed() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg",1L, 50000);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.CLOSED);
        LocalDateTime originalClosedAt = LocalDateTime.now().minusDays(1);
        ReflectionTestUtils.setField(funding, "closedAt", originalClosedAt);

        // when
        funding.close();

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.CLOSED);
        // closedAt이 갱신되지 않아야 함
        assertThat(funding.getClosedAt()).isEqualTo(originalClosedAt);
    }

    @Test
    @DisplayName("close 멱등성: 이미 만료된 펀딩은 상태 변경 없음")
    void close_Idempotency_AlreadyExpired() {
        // given
        Funding funding = Funding.startFunding(1L, 1L, "상품이름", "products/51/chanel-perfume.jpg",1L, 50000);
        ReflectionTestUtils.setField(funding, "status", FundingStatus.EXPIRED);

        // when
        funding.close();

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.EXPIRED);
    }
}
