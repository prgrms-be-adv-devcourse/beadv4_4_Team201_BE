package app.giftify.funding;

import app.giftify.app.funding.FundingSyncItemUseCase;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.WishlistItemDto;
import app.giftify.out.FundingWishlistItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingSyncItemUseCaseTest {

    @Mock
    private FundingWishlistItemRepository fundingWishlistItemRepository;

    @InjectMocks
    private FundingSyncItemUseCase fundingSyncItemUseCase;

    // ===== 테스트 헬퍼 메서드 =====

    private WishlistItemDto createTestWishlistItemDto() {
        return new WishlistItemDto(
                1L,      // wishlistItemId
                100L,    // productId
                "테스트 상품",  // productName
                50000    // productPrice
        );
    }

    // ===== syncItem 테스트 =====

    @Test
    @DisplayName("syncItem - WishlistItem 스냅샷 생성 성공")
    void syncItem_success() {
        // given
        WishlistItemDto dto = createTestWishlistItemDto();

        when(fundingWishlistItemRepository.save(any(FundingWishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FundingWishlistItem result = fundingSyncItemUseCase.syncItem(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWishlistId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(100L);
        assertThat(result.getProductName()).isEqualTo("테스트 상품");
        assertThat(result.getProductPrice()).isEqualTo(50000);
        assertThat(result.getStatus()).isEqualTo(FundingWishlistItem.WishListItemStatus.IN_PROGRESS);

        verify(fundingWishlistItemRepository, times(1)).save(any(FundingWishlistItem.class));
    }

    @Test
    @DisplayName("syncItem - 스냅샷 상태는 항상 IN_PROGRESS")
    void syncItem_always_creates_in_progress_status() {
        // given
        WishlistItemDto dto = createTestWishlistItemDto();

        when(fundingWishlistItemRepository.save(any(FundingWishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FundingWishlistItem result = fundingSyncItemUseCase.syncItem(dto);

        // then
        assertThat(result.getStatus()).isEqualTo(FundingWishlistItem.WishListItemStatus.IN_PROGRESS);

        verify(fundingWishlistItemRepository, times(1)).save(any(FundingWishlistItem.class));
    }

    @Test
    @DisplayName("syncItem - DTO의 모든 필드가 정확히 복사됨")
    void syncItem_copies_all_fields_from_dto() {
        // given
        WishlistItemDto dto = new WishlistItemDto(
                999L,           // wishlistItemId
                777L,           // productId
                "특별한 상품명",   // productName
                123456          // productPrice
        );

        when(fundingWishlistItemRepository.save(any(FundingWishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FundingWishlistItem result = fundingSyncItemUseCase.syncItem(dto);

        // then
        assertThat(result.getWishlistId()).isEqualTo(999L);
        assertThat(result.getProductId()).isEqualTo(777L);
        assertThat(result.getProductName()).isEqualTo("특별한 상품명");
        assertThat(result.getProductPrice()).isEqualTo(123456);

        verify(fundingWishlistItemRepository, times(1)).save(any(FundingWishlistItem.class));
    }

    @Test
    @DisplayName("syncItem - repository save 호출 검증")
    void syncItem_calls_repository_save() {
        // given
        WishlistItemDto dto = createTestWishlistItemDto();

        when(fundingWishlistItemRepository.save(any(FundingWishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        fundingSyncItemUseCase.syncItem(dto);

        // then
        verify(fundingWishlistItemRepository, times(1)).save(argThat(item ->
                item.getWishlistId().equals(1L) &&
                item.getProductId().equals(100L) &&
                item.getProductName().equals("테스트 상품") &&
                item.getProductPrice() == 50000 &&
                item.getStatus() == FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        ));
    }

    @Test
    @DisplayName("syncItem - 저장된 엔티티 반환 검증")
    void syncItem_returns_saved_entity() {
        // given
        WishlistItemDto dto = createTestWishlistItemDto();
        FundingWishlistItem savedItem = new FundingWishlistItem(
                dto.wishlistItemId(),
                dto.productId(),
                dto.productName(),
                dto.productPrice(),
                FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        );

        when(fundingWishlistItemRepository.save(any(FundingWishlistItem.class)))
                .thenReturn(savedItem);

        // when
        FundingWishlistItem result = fundingSyncItemUseCase.syncItem(dto);

        // then
        assertThat(result).isEqualTo(savedItem);
        verify(fundingWishlistItemRepository, times(1)).save(any(FundingWishlistItem.class));
    }

    @Test
    @DisplayName("syncItem - 상품 가격이 0원인 경우도 처리 가능")
    void syncItem_handles_zero_price() {
        // given
        WishlistItemDto dto = new WishlistItemDto(
                1L,
                100L,
                "무료 상품",
                0  // 0원
        );

        when(fundingWishlistItemRepository.save(any(FundingWishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FundingWishlistItem result = fundingSyncItemUseCase.syncItem(dto);

        // then
        assertThat(result.getProductPrice()).isEqualTo(0);
        verify(fundingWishlistItemRepository, times(1)).save(any(FundingWishlistItem.class));
    }

    @Test
    @DisplayName("syncItem - 상품명이 긴 경우도 처리 가능")
    void syncItem_handles_long_product_name() {
        // given
        String longProductName = "A".repeat(100); // 100자
        WishlistItemDto dto = new WishlistItemDto(
                1L,
                100L,
                longProductName,
                50000
        );

        when(fundingWishlistItemRepository.save(any(FundingWishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FundingWishlistItem result = fundingSyncItemUseCase.syncItem(dto);

        // then
        assertThat(result.getProductName()).isEqualTo(longProductName);
        assertThat(result.getProductName()).hasSize(100);
        verify(fundingWishlistItemRepository, times(1)).save(any(FundingWishlistItem.class));
    }
}

