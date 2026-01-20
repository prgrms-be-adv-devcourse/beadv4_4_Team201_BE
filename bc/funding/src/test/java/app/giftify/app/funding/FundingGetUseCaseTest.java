package app.giftify.app.funding;

import app.giftify.domain.funding.*;
import app.giftify.in.funding.FundingResponseDto;
import app.giftify.in.funding.MyFundingResponseDto;
import app.giftify.out.funding.FundingParticipantMemberRepository;
import app.giftify.out.funding.FundingRepository;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.support.jpa.BaseJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingGetUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingParticipantMemberRepository participantMemberRepository;

    @InjectMocks
    private FundingGetUseCase fundingGetUseCase;

    // ===== 테스트 헬퍼 메서드 =====

    private FundingWishlistItem createTestWishlistItem() {
        return new FundingWishlistItem(
                1L,      // wishlistId
                999L,    // receiverId
                100L,    // productId
                "테스트 상품",
                50000,
                FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        );
    }

    private void setEntityId(BaseJpaEntity entity, Long id) {
        try {
            Field idField = BaseJpaEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID", e);
        }
    }

    // ===== getFunding 테스트 =====

    @Test
    @DisplayName("getFunding - 진행 중인 펀딩 조회 성공")
    void getFunding_success_when_in_progress() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        setEntityId(funding, fundingId);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingResponseDto result = fundingGetUseCase.getFunding(fundingId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fundingId()).isEqualTo(funding.getId());
        assertThat(result.targetAmount()).isEqualTo(50000);
        assertThat(result.currentAmount()).isEqualTo(10000);
        assertThat(result.status()).isEqualTo(FundingStatus.IN_PROGRESS);
        assertThat(result.productId()).isEqualTo(100L);
        assertThat(result.productName()).isEqualTo("테스트 상품");
        assertThat(result.productPrice()).isEqualTo(50000);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 목표 달성 펀딩 조회 성공")
    void getFunding_success_when_achieved() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000); // 첫 결제 10,000원
        setEntityId(funding, fundingId);
        
        // contribute 전 상태 확인
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS);
        assertThat(funding.getCurrentAmount()).isEqualTo(10000);
        
        funding.contribute(40000); // 추가로 40,000원 결제 → 목표 달성
        
        // contribute 후 상태 확인 (디버깅용)
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(funding.getCurrentAmount()).isEqualTo(50000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingResponseDto result = fundingGetUseCase.getFunding(fundingId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(result.currentAmount()).isEqualTo(result.targetAmount());

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 펀딩을 찾을 수 없는 경우 예외 발생")
    void getFunding_fail_when_funding_not_found() {
        // given
        Long fundingId = 999L;
        when(fundingRepository.findById(fundingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FUNDING_NOT_FOUND);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 종료된 펀딩은 조회 불가")
    void getFunding_fail_when_funding_closed() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        setEntityId(funding, fundingId);
        funding.close(); // 종료 처리

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_IN_PROGRESS);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 만료된 펀딩은 조회 불가")
    void getFunding_fail_when_funding_expired() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        setEntityId(funding, fundingId);

        // deadline을 과거로 설정 (리플렉션)
        try {
            Field deadlineField = Funding.class.getDeclaredField("deadline");
            deadlineField.setAccessible(true);
            deadlineField.set(funding, LocalDateTime.now().minusDays(1));
        } catch (Exception e) {
            // 필드명이 다를 수 있으므로 무시하거나 로그
        }
        
        try {
             funding.expire(); // 만료 처리
        } catch (Exception e) {
            // expire 조건이 안맞으면 예외 발생 가능
        }
        
        // 강제로 상태 변경 (확실한 테스트를 위해)
        try {
            Field statusField = Funding.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(funding, FundingStatus.EXPIRED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_IN_PROGRESS);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    // ===== getParticipatedFunding 테스트 =====

    @Test
    @DisplayName("getParticipatedFunding - 내가 참여한 펀딩 조회 성공")
    void getParticipatedFunding_success() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        setEntityId(funding, fundingId);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));
        when(participantMemberRepository.existsByFundingIdAndFundingMemberId(fundingId, memberId)).thenReturn(true);
        when(participantMemberRepository.findTotalAmountByFundingIdAndMemberId(fundingId, memberId)).thenReturn(Optional.of(5000));

        // when
        MyFundingResponseDto result = fundingGetUseCase.getParticipatedFunding(fundingId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fundingId()).isEqualTo(fundingId);
        assertThat(result.myContribution()).isEqualTo(5000);
        assertThat(result.currentAmount()).isEqualTo(10000);

        verify(fundingRepository).findById(fundingId);
        verify(participantMemberRepository).existsByFundingIdAndFundingMemberId(fundingId, memberId);
        verify(participantMemberRepository).findTotalAmountByFundingIdAndMemberId(fundingId, memberId);
    }

    @Test
    @DisplayName("getParticipatedFunding - 참여하지 않은 펀딩 조회 시 예외 발생")
    void getParticipatedFunding_fail_when_not_participated() {
        // given
        Long fundingId = 1L;
        Long memberId = 100L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        setEntityId(funding, fundingId);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));
        when(participantMemberRepository.existsByFundingIdAndFundingMemberId(fundingId, memberId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> fundingGetUseCase.getParticipatedFunding(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FORBIDDEN);

        verify(fundingRepository).findById(fundingId);
        verify(participantMemberRepository).existsByFundingIdAndFundingMemberId(fundingId, memberId);
        verify(participantMemberRepository, never()).findTotalAmountByFundingIdAndMemberId(any(), any());
    }

    // ===== getFundings 테스트 =====

    @Test
    @DisplayName("getFundings - 펀딩 리스트 조회 성공")
    void getFundings_success() {
        // given
        int page = 0;
        int size = 10;
        FundingWishlistItem item1 = createTestWishlistItem();
        Funding funding1 = Funding.startFunding(item1, 10000);
        setEntityId(funding1, 1L);
        
        FundingWishlistItem item2 = createTestWishlistItem();
        Funding funding2 = Funding.startFunding(item2, 20000);
        setEntityId(funding2, 2L);

        List<Funding> fundingList = List.of(funding1, funding2);
        Page<Funding> fundingPage = new PageImpl<>(fundingList, PageRequest.of(page, size), fundingList.size());

        when(fundingRepository.findAllByStatusIn(any(), any(Pageable.class))).thenReturn(fundingPage);

        // when
        PageResponse<FundingResponseDto> result = fundingGetUseCase.getFundings(page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.pageNumber()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(10);
        
        // 첫 번째 항목 검증
        assertThat(result.content().get(0).currentAmount()).isEqualTo(10000);
        // 두 번째 항목 검증
        assertThat(result.content().get(1).currentAmount()).isEqualTo(20000);

        verify(fundingRepository).findAllByStatusIn(any(), any(Pageable.class));
    }
}
