package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import app.giftify.shared.domain.type.FundingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingExpireUseCaseTest {

    @InjectMocks
    private FundingExpireUseCase fundingExpireUseCase;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private FundingParticipantMemberRepository fundingParticipantMemberRepository;

    @Test
    @DisplayName("expireFunding 성공: 펀딩 만료 처리")
    void expireFunding_Success() {
        // given
        Long fundingId = 1L;
        Funding funding = mock(Funding.class);

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
        given(funding.expire(any(LocalDateTime.class))).willReturn(true);
        given(funding.getId()).willReturn(fundingId);
        given(funding.getWishlistItemId()).willReturn(10L);
        given(funding.getCurrentAmount()).willReturn(5000);
        given(funding.getStatus()).willReturn(FundingStatus.EXPIRED);
        given(funding.getClosedAt()).willReturn(LocalDateTime.now());
        given(fundingParticipantMemberRepository.findIdsByFundingId(fundingId)).willReturn(Collections.emptyList());

        // when
        FundingCompleteResponseDto result = fundingExpireUseCase.expireFunding(fundingId);

        // then
        assertThat(result.status()).isEqualTo(FundingStatus.EXPIRED);
        verify(eventPublisher).publish(any(FundingExpiredEvent.class));
    }

    @Test
    @DisplayName("expireFunding 멱등성: 이미 만료된 펀딩은 이벤트 발행 안함")
    void expireFunding_Idempotency_AlreadyExpired() {
        // given
        Long fundingId = 1L;
        Funding funding = mock(Funding.class);

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
        // expire()가 false를 반환하도록 설정 (이미 만료/종료된 경우)
        given(funding.expire(any(LocalDateTime.class))).willReturn(false);
        given(funding.getId()).willReturn(fundingId);
        given(funding.getWishlistItemId()).willReturn(10L);
        given(funding.getStatus()).willReturn(FundingStatus.EXPIRED);
        given(funding.getClosedAt()).willReturn(LocalDateTime.now());

        // when
        FundingCompleteResponseDto result = fundingExpireUseCase.expireFunding(fundingId);

        // then
        assertThat(result.status()).isEqualTo(FundingStatus.EXPIRED);
        // 이벤트가 발행되지 않아야 함
        verify(eventPublisher, never()).publish(any(FundingExpiredEvent.class));
    }

    @Test
    @DisplayName("expireFunding 실패: 펀딩을 찾을 수 없음")
    void expireFunding_Fail_FundingNotFound() {
        // given
        Long fundingId = 1L;
        given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingExpireUseCase.expireFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.FUNDING_NOT_FOUND);
    }
}
