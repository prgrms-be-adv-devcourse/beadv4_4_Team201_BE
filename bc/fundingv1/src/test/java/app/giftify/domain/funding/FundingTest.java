package app.giftify.domain.funding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class FundingTest {

    // ===== 테스트 헬퍼 메서드 =====

    private FundingWishlistItem createTestWishlistItem(Long wishlistId, Long productId, String productName, int productPrice) {
        return new FundingWishlistItem(
                wishlistId,
                999L,  // receiverId
                productId,
                productName,
                productPrice,
                FundingWishlistItem.WishListItemStatus.PENDING
        );
    }

    // ===== validateAmount 테스트 =====

    @Test
    @DisplayName("validateAmount - 금액이 null이면 예외 발생")
    void validateAmount_fail_when_Least_amount_is_null() {
        // when & then
        assertThatThrownBy(() -> Funding.validateLeastAmount(null))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");
    }

    @Test
    @DisplayName("validateAmount - 금액이 1000원 미만이면 예외 발생")
    void validateAmount_fail_when_Least_amount_less_than_1000() {
        // when & then
        assertThatThrownBy(() -> Funding.validateLeastAmount(999))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");

        assertThatThrownBy(() -> Funding.validateLeastAmount(0))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");

        assertThatThrownBy(() -> Funding.validateLeastAmount(-100))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("1,000원");
    }

    @Test
    @DisplayName("validateAmount - 금액이 1000원 이상이면 성공")
    void validateAmount_success_when_Least_amount_greater_or_equal_1000() {
        // when & then - 예외 발생하지 않음
        assertThatCode(() -> {
            Funding.validateLeastAmount(1000);
            Funding.validateLeastAmount(5000);
            Funding.validateLeastAmount(100000);
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
        assertThat(funding.getDeadline()).isNotNull();
    }

    @Test
    @DisplayName("startFunding - 첫 결제 금액이 목표 금액과 같으면 ACHIEVED 상태")
    void startFunding_achieved_when_amount_equals_target() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);

        // when
        Funding funding = Funding.startFunding(item, 50000);

        // then
        assertThat(funding).isNotNull();
        assertThat(funding.getTargetAmount()).isEqualTo(50000);
        assertThat(funding.getCurrentAmount()).isEqualTo(50000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED); // 바로 달성!
        assertThat(funding.isAchieved()).isTrue();
        assertThat(funding.getFundingWishlistItem()).isEqualTo(item);
        assertThat(funding.getDeadline()).isNotNull();
    }

    @Test
    @DisplayName("startFunding - 첫 결제 금액이 목표 금액보다 적으면 IN_PROGRESS 상태")
    void startFunding_in_progress_when_amount_less_than_target() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);

        // when
        Funding funding = Funding.startFunding(item, 30000);

        // then
        assertThat(funding).isNotNull();
        assertThat(funding.getCurrentAmount()).isEqualTo(30000);
        assertThat(funding.getTargetAmount()).isEqualTo(50000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS); // 아직 진행 중
        assertThat(funding.isAchieved()).isFalse();
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
    @DisplayName("expire - 이미 종료된 펀딩(CLOSED)은 만료 불가")
    void expire_fail_when_already_closed() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);
        funding.close(); // 먼저 종료

        // when & then
        assertThatThrownBy(() -> funding.expire())
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("완료");
    }

    // ===== close 테스트 =====

    @Test
    @DisplayName("close - 진행 중인 펀딩 강제 종료 성공")
    void close_success_when_in_progress() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);

        // when
        funding.close();

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.CLOSED);
        assertThat(funding.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("close - 목표 달성 펀딩도 종료 가능")
    void close_success_when_achieved() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000); // 첫 결제 10,000원
        funding.contribute(40000); // 추가로 40,000원 → 목표 달성

        // when
        funding.close();

        // then
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.CLOSED);
        assertThat(funding.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("close - 이미 종료된 펀딩은 재종료 불가")
    void close_fail_when_already_closed() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);
        funding.close(); // 먼저 종료

        // when & then
        assertThatThrownBy(() -> funding.close())
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("완료");
    }

    @Test
    @DisplayName("close - 이미 만료된 펀딩은 종료 불가")
    void close_fail_when_already_expired() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);

        // endAt을 과거로 설정하기 위해 리플렉션 사용
        try {
            Field deadlineField = Funding.class.getDeclaredField("deadline");
            deadlineField.setAccessible(true);
            deadlineField.set(funding, LocalDateTime.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        funding.expire(); // 만료 처리

        // when & then
        assertThatThrownBy(() -> funding.close())
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("완료");
    }

    // ===== 상태 전환 통합 테스트 =====

    @Test
    @DisplayName("contribute 후 CLOSED 상태에서는 추가 contribute 불가")
    void contribute_fail_after_closed() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000);
        funding.close();

        // when & then
        assertThatThrownBy(() -> funding.contribute(5000))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("진행 중");
    }

    @Test
    @DisplayName("ACHIEVED 상태에서는 추가 contribute 불가")
    void contribute_fail_after_achieved() {
        // given
        FundingWishlistItem item = createTestWishlistItem(1L, 100L, "테스트 상품", 50000);
        Funding funding = Funding.startFunding(item, 10000); // 첫 결제 10,000원
        funding.contribute(40000); // 추가로 40,000원 → 목표 달성

        // when & then
        assertThatThrownBy(() -> funding.contribute(1000))
                .isInstanceOf(FundingException.class)
                .hasMessageContaining("진행 중");
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
        Funding funding2 = Funding.startFunding(item, 10000); // 첫 결제 10,000원
        funding2.contribute(40000); // 추가로 40,000원 → 목표 달성

        // then
        assertThat(funding2.isAchieved()).isTrue();
    }
}
