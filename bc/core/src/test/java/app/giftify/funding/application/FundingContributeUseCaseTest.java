package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.jpa.FundingParticipantMember;
import app.giftify.funding.adpater.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingContributeUseCaseTest {

    @InjectMocks
    private FundingContributeUseCase fundingContributeUseCase;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingParticipantMemberRepository fundingParticipantMemberRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("contribute 성공: 기존 참여자가 없을 때")
    void contribute_Success_NewParticipant() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;

        Funding funding = mock(Funding.class);
        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
        given(fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId)).willReturn(null);

        // when
        Funding result = fundingContributeUseCase.contribute(fundingId, participantId, amount);

        // then
        assertThat(result).isEqualTo(funding);
        verify(fundingParticipantMemberRepository).save(any(FundingParticipantMember.class));
        verify(funding).contribute(amount);
    }

    @Test
    @DisplayName("contribute 성공: 기존 참여자가 있을 때")
    void contribute_Success_ExistingParticipant() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;

        Funding funding = mock(Funding.class);
        FundingParticipantMember member = mock(FundingParticipantMember.class);

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
        given(fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId)).willReturn(member);

        // when
        Funding result = fundingContributeUseCase.contribute(fundingId, participantId, amount);

        // then
        assertThat(result).isEqualTo(funding);
        verify(member).addAmount(amount);
        verify(funding).contribute(amount);
    }

    @Test
    @DisplayName("contribute 성공: 펀딩 달성 시 이벤트 발행")
    void contribute_Success_FundingAchieved() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;

        Funding funding = mock(Funding.class);
        given(funding.getId()).willReturn(fundingId);
        given(funding.getWishlistItemId()).willReturn(10L);
        given(funding.isAchieved()).willReturn(true);

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(funding));
        given(fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId)).willReturn(null);

        // when
        fundingContributeUseCase.contribute(fundingId, participantId, amount);

        // then
        verify(eventPublisher).publish(any(FundingAchievedEvent.class));
    }

    @Test
    @DisplayName("contribute 실패: 펀딩을 찾을 수 없음")
    void contribute_Fail_FundingNotFound() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;

        given(fundingRepository.findById(fundingId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingContributeUseCase.contribute(fundingId, participantId, amount))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.FUNDING_NOT_FOUND);
    }
}
