package app.giftify.funding.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
class WithdrawFundingUseCaseTest {

    @InjectMocks
    private WithdrawFundingUseCase withdrawFundingUseCase;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingParticipantMemberRepository fundingParticipantMemberRepository;

    private final Long wishlistItemId = 1L;
    private final Long participantId = 100L;
    private final Long fundingId = 10L;
    private final Money amount = Money.of(5000);

    @Test
    @DisplayName("펀딩 철회 성공: participant 존재 시 금액 차감 + 참여자 삭제")
    void withdraw_Success() {
        // given
        Funding funding = mock(Funding.class);
        given(funding.getId()).willReturn(fundingId);
        given(fundingRepository.findActiveByWishlistItemId(wishlistItemId))
                .willReturn(Optional.of(funding));
        given(fundingParticipantMemberRepository.existsByFundingIdAndParticipantId(fundingId, participantId))
                .willReturn(true);

        // when
        withdrawFundingUseCase.withdrawByWishlistItem(wishlistItemId, participantId, amount);

        // then
        then(funding).should().withdraw(5000);
        then(fundingParticipantMemberRepository).should()
                .deleteByFundingIdAndParticipantId(fundingId, participantId);
    }

    @Test
    @DisplayName("펀딩 철회 멱등성: participant 이미 삭제됐으면 아무 작업 없이 정상 리턴")
    void withdraw_Idempotent_ParticipantAlreadyDeleted() {
        // given
        Funding funding = mock(Funding.class);
        given(funding.getId()).willReturn(fundingId);
        given(fundingRepository.findActiveByWishlistItemId(wishlistItemId))
                .willReturn(Optional.of(funding));
        given(fundingParticipantMemberRepository.existsByFundingIdAndParticipantId(fundingId, participantId))
                .willReturn(false);

        // when
        withdrawFundingUseCase.withdrawByWishlistItem(wishlistItemId, participantId, amount);

        // then
        then(funding).should(never()).withdraw(anyInt());
        then(fundingParticipantMemberRepository).should(never())
                .deleteByFundingIdAndParticipantId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("펀딩 철회 실패: 활성 펀딩 없음")
    void withdraw_Fail_FundingNotFound() {
        // given
        given(fundingRepository.findActiveByWishlistItemId(wishlistItemId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                withdrawFundingUseCase.withdrawByWishlistItem(wishlistItemId, participantId, amount))
                .isInstanceOf(FundingException.class);
    }
}
