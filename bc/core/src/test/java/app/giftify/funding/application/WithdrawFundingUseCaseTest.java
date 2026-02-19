package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.FundingStatus;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WithdrawFundingUseCaseTest {

    @InjectMocks
    private WithdrawFundingUseCase withdrawFundingUseCase;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingParticipantMemberRepository fundingParticipantMemberRepository;

    @Test
    @DisplayName("펀딩 기여금 출금에 성공한다.")
    void withdrawFunding_Success() {
        // given
        Long fundingId = 1L;
        Long participantId = 1L;
        Money amount = Money.of(10000);
        Funding mockFunding = Funding.startFunding(1L, 1L, 1L, 50000);
        mockFunding.contribute(20000); // 초기 기여금 설정

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(mockFunding));

        // when
        withdrawFundingUseCase.withdrawFunding(fundingId, participantId, amount);

        // then
        then(fundingRepository).should(times(1)).findById(fundingId);
        then(fundingParticipantMemberRepository).should(times(1)).deleteByFundingIdAndParticipantId(fundingId, participantId);
        
        // 펀딩 금액이 정상적으로 차감되었는지 확인 (실제 객체의 상태를 검증)
        org.assertj.core.api.Assertions.assertThat(mockFunding.getCurrentAmount()).isEqualTo(10000);
    }

    @Test
    @DisplayName("존재하지 않는 펀딩 ID로 출금 시 예외가 발생한다.")
    void withdrawFunding_Fail_FundingNotFound() {
        // given
        Long nonExistentFundingId = 999L;
        Long participantId = 1L;
        Money amount = Money.of(10000);

        given(fundingRepository.findById(nonExistentFundingId)).willReturn(Optional.empty());

        // when & then
        assertThrows(FundingException.class, () -> {
            withdrawFundingUseCase.withdrawFunding(nonExistentFundingId, participantId, amount);
        });

        then(fundingRepository).should(times(1)).findById(nonExistentFundingId);
        then(fundingParticipantMemberRepository).should(times(0)).deleteByFundingIdAndParticipantId(any(), any());
    }

    @Test
    @DisplayName("펀딩이 진행 중이거나 달성 상태가 아니면 출금할 수 없다.")
    void withdrawFunding_Fail_InvalidStatus() throws Exception {
        // given
        Long fundingId = 1L;
        Long participantId = 1L;
        Money amount = Money.of(10000);
        Funding mockFunding = Funding.startFunding(1L, 1L, 1L, 50000);
        
        // 리플렉션을 사용하여 status 필드를 EXPIRED로 변경
        Field statusField = Funding.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(mockFunding, FundingStatus.EXPIRED);

        given(fundingRepository.findById(fundingId)).willReturn(Optional.of(mockFunding));

        // when & then
        FundingException exception = assertThrows(FundingException.class, () -> {
            withdrawFundingUseCase.withdrawFunding(fundingId, participantId, amount);
        });
        
        // 에러 코드가 INVALID_STATUS_FOR_WITHDRAWAL인지 확인
        org.assertj.core.api.Assertions.assertThat(exception.getErrorCode()).isEqualTo(FundingErrorCode.INVALID_STATUS_FOR_WITHDRAWAL);

        then(fundingRepository).should(times(1)).findById(fundingId);
        then(fundingParticipantMemberRepository).should(times(0)).deleteByFundingIdAndParticipantId(any(), any());
    }
}
