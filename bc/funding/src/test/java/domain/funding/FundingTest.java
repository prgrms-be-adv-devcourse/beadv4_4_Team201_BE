package domain.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.domain.funding.FundingWishlistItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FundingTest {

    // ===== 테스트 헬퍼 메서드 =====

    private FundingWishlistItem createTestWishlistItem(Long wishlistId, Long productId, String productName, int productPrice) {
        return new FundingWishlistItem(
                wishlistId,
                productId,
                productName,
                productPrice,
                FundingWishlistItem.WishListItemStatus.PENDING
        );
    }

    // ===== validateAmount 테스트 =====

    @Test
    @DisplayName("validateAmount - 금액이 null이면 예외 발생")
    void validateAmount_fail_when_amount_is_null() {
        // when & then
        assertThatThrownBy(() -> Funding.validateAmount(null))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");
    }

    @Test
    @DisplayName("validateAmount - 금액이 1000원 미만이면 예외 발생")
    void validateAmount_fail_when_amount_less_than_1000() {
        // when & then
        assertThatThrownBy(() -> Funding.validateAmount(999))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");

        assertThatThrownBy(() -> Funding.validateAmount(0))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");

        assertThatThrownBy(() -> Funding.validateAmount(-100))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");
    }

    @Test
    @DisplayName("validateAmount - 금액이 1000원 이상이면 성공")
    void validateAmount_success_when_amount_greater_or_equal_1000() {
        // when & then - 예외 발생하지 않음
        assertThatCode(() -> {
            Funding.validateAmount(1000);
            Funding.validateAmount(5000);
            Funding.validateAmount(100000);
        }).doesNotThrowAnyException();
    }

    // ===== startFunding 테스트 =====

    @Test
    @DisplayName("startFunding - 금액이 1000원 미만이면 예외 발생")
    void startFunding_fail_when_amount_less_than_1000() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);

        // when & then
        assertThatThrownBy(() -> Funding.startFunding(item, 500))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");
    }

    @Test
    @DisplayName("startFunding - 정상 생성 시 초기 상태 확인")
    void startFunding_success_with_valid_amount() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);

        // when
        Funding funding = Funding.startFunding(item, 10000);

        // then
        assertThat(funding).isNotNull();
        assertThat(funding.getTargetAmount()).isEqualTo(50000); // 상품 가격
        assertThat(funding.getCurrentAmount()).isEqualTo(10000); // 초기 금액
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS);
        assertThat(funding.getFundingWishlistItem()).isEqualTo(item);
        assertThat(funding.getEndAt()).isNotNull();
    }

    // ===== contribute 테스트 =====

    @Test
    @DisplayName("contribute - 진행 중인 펀딩에 참여 성공")
    void contribute_success() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);

        // when
        funding.contribute(5000);

        // then
        assertThat(funding.getCurrentAmount()).isEqualTo(15000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("contribute - 목표 금액 달성 시 상태 변경")
    void contribute_achieve_target_amount() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);

        // when
        funding.contribute(40000); // 총 50000원 달성

        // then
        assertThat(funding.getCurrentAmount()).isEqualTo(50000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(funding.isAchieved()).isTrue();
    }

    @Test
    @DisplayName("contribute - 목표 금액 초과 시 예외 발생")
    void contribute_fail_when_exceed_target_amount() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000); // 현재 10000원, 잔여 40000원

        // when & then
        assertThatThrownBy(() -> funding.contribute(50000)) // 40000원 초과 시도
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("잔여 금액")
                .hasMessageContaining("40000");
    }

    @Test
    @DisplayName("contribute - 정확히 목표 금액만큼 기부 시 성공")
    void contribute_success_with_exact_remaining_amount() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000); // 현재 10000원, 잔여 40000원

        // when
        funding.contribute(40000); // 정확히 잔여 금액만큼

        // then
        assertThat(funding.getCurrentAmount()).isEqualTo(50000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(funding.isAchieved()).isTrue();
    }

    @Test
    @DisplayName("contribute - 1000원 미만 금액으로 참여 시 예외")
    void contribute_fail_when_amount_less_than_1000() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);

        // when & then
        assertThatThrownBy(() -> funding.contribute(500))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");
    }

    // ===== expire 테스트 =====

    @Test
    @DisplayName("expire - 진행 중인 펀딩 만료 처리")
    void expire_success() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);

        // when
        funding.expire();

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.EXPIRED);
    }

    // ===== isAchieved 테스트 =====

    @Test
    @DisplayName("isAchieved - 목표 금액 달성 여부 확인")
    void isAchieved_test() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);

        // when - 목표 미달
        Funding funding1 = Funding.startFunding(item, 10000);

        // then
        assertThat(funding1.isAchieved()).isFalse();

        // when - 목표 달성
        Funding funding2 = Funding.startFunding(item, 50000);

        // then
        assertThat(funding2.isAchieved()).isTrue();
    }
}
