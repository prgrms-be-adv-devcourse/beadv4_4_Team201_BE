package app.giftify.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PaymentEventType의 상태 전이 규칙을 검증합니다.
 */
class PaymentEventTypeTest {

	@Nested
	@DisplayName("Given CREATED 이벤트")
	class Given_CREATED_이벤트 {

		@Nested
		@DisplayName("When canApply를 호출하면")
		class When_canApply를_호출하면 {

			@Test
			@DisplayName("Then currentStatus가 null일 때만 true를 반환한다")
			void Then_currentStatus가_null일_때만_true를_반환한다() {
				assertThat(PaymentEventType.CREATED.canApply(null)).isTrue();
			}

			@ParameterizedTest
			@EnumSource(PaymentStatus.class)
			@DisplayName("Then currentStatus가 존재하면 false를 반환한다")
			void Then_currentStatus가_존재하면_false를_반환한다(PaymentStatus status) {
				assertThat(PaymentEventType.CREATED.canApply(status)).isFalse();
			}
		}

		@Nested
		@DisplayName("When getResultStatus를 호출하면")
		class When_getResultStatus를_호출하면 {

			@Test
			@DisplayName("Then PENDING을 반환한다")
			void Then_PENDING을_반환한다() {
				assertThat(PaymentEventType.CREATED.getResultStatus()).isEqualTo(PaymentStatus.PENDING);
			}
		}
	}

	@Nested
	@DisplayName("Given PENDING 상태에서 적용 가능한 이벤트")
	class Given_PENDING_상태에서_적용_가능한_이벤트 {

		@Nested
		@DisplayName("When PAID 이벤트를 적용하면")
		class When_PAID_이벤트를_적용하면 {

			@Test
			@DisplayName("Then PENDING 상태에서만 적용 가능하다")
			void Then_PENDING_상태에서만_적용_가능하다() {
				assertThat(PaymentEventType.PAID.canApply(PaymentStatus.PENDING)).isTrue();
				assertThat(PaymentEventType.PAID.canApply(PaymentStatus.PAID)).isFalse();
				assertThat(PaymentEventType.PAID.canApply(null)).isFalse();
			}

			@Test
			@DisplayName("Then 결과 상태는 PAID이다")
			void Then_결과_상태는_PAID이다() {
				assertThat(PaymentEventType.PAID.getResultStatus()).isEqualTo(PaymentStatus.PAID);
			}
		}

		@Nested
		@DisplayName("When FAILED 이벤트를 적용하면")
		class When_FAILED_이벤트를_적용하면 {

			@Test
			@DisplayName("Then PENDING 상태에서만 적용 가능하다")
			void Then_PENDING_상태에서만_적용_가능하다() {
				assertThat(PaymentEventType.FAILED.canApply(PaymentStatus.PENDING)).isTrue();
				assertThat(PaymentEventType.FAILED.canApply(PaymentStatus.PAID)).isFalse();
			}

			@Test
			@DisplayName("Then 결과 상태는 FAILED이다")
			void Then_결과_상태는_FAILED이다() {
				assertThat(PaymentEventType.FAILED.getResultStatus()).isEqualTo(PaymentStatus.FAILED);
			}
		}

		@Nested
		@DisplayName("When CANCELED 이벤트를 적용하면")
		class When_CANCELED_이벤트를_적용하면 {

			@Test
			@DisplayName("Then PENDING 상태에서만 적용 가능하다")
			void Then_PENDING_상태에서만_적용_가능하다() {
				assertThat(PaymentEventType.CANCELED.canApply(PaymentStatus.PENDING)).isTrue();
				assertThat(PaymentEventType.CANCELED.canApply(PaymentStatus.PAID)).isFalse();
			}

			@Test
			@DisplayName("Then 결과 상태는 CANCELED이다")
			void Then_결과_상태는_CANCELED이다() {
				assertThat(PaymentEventType.CANCELED.getResultStatus()).isEqualTo(PaymentStatus.CANCELED);
			}
		}
	}

	@Nested
	@DisplayName("Given PAID 상태에서 적용 가능한 이벤트")
	class Given_PAID_상태에서_적용_가능한_이벤트 {

		@Nested
		@DisplayName("When RECEIVED 이벤트를 적용하면")
		class When_RECEIVED_이벤트를_적용하면 {

			@Test
			@DisplayName("Then PAID 상태에서만 적용 가능하다")
			void Then_PAID_상태에서만_적용_가능하다() {
				assertThat(PaymentEventType.RECEIVED.canApply(PaymentStatus.PAID)).isTrue();
				assertThat(PaymentEventType.RECEIVED.canApply(PaymentStatus.PENDING)).isFalse();
			}

			@Test
			@DisplayName("Then 결과 상태는 RECEIVED이다")
			void Then_결과_상태는_RECEIVED이다() {
				assertThat(PaymentEventType.RECEIVED.getResultStatus()).isEqualTo(PaymentStatus.RECEIVED);
			}
		}

		@Nested
		@DisplayName("When REFUNDED 이벤트를 적용하면")
		class When_REFUNDED_이벤트를_적용하면 {

			@Test
			@DisplayName("Then PAID 상태에서만 적용 가능하다")
			void Then_PAID_상태에서만_적용_가능하다() {
				assertThat(PaymentEventType.REFUNDED.canApply(PaymentStatus.PAID)).isTrue();
				assertThat(PaymentEventType.REFUNDED.canApply(PaymentStatus.PENDING)).isFalse();
				assertThat(PaymentEventType.REFUNDED.canApply(PaymentStatus.RECEIVED)).isFalse();
			}

			@Test
			@DisplayName("Then 결과 상태는 REFUNDED이다")
			void Then_결과_상태는_REFUNDED이다() {
				assertThat(PaymentEventType.REFUNDED.getResultStatus()).isEqualTo(PaymentStatus.REFUNDED);
			}
		}
	}

	@Nested
	@DisplayName("Given 모든 이벤트에 대해")
	class Given_모든_이벤트에_대해 {

		@ParameterizedTest
		@EnumSource(PaymentEventType.class)
		@DisplayName("When changesState를 호출하면 Then 상태 변경 여부를 정확히 반환한다")
		void When_changesState를_호출하면_Then_상태_변경_여부를_정확히_반환한다(PaymentEventType eventType) {
			// 현재 모든 이벤트는 상태를 변경함
			assertThat(eventType.changesState()).isTrue();
		}

		@ParameterizedTest
		@EnumSource(PaymentEventType.class)
		@DisplayName("When getFromStatus를 호출하면 Then 시작 상태를 반환한다")
		void When_getFromStatus를_호출하면_Then_시작_상태를_반환한다(PaymentEventType eventType) {
			PaymentStatus fromStatus = eventType.getFromStatus();
			PaymentStatus toStatus = eventType.getResultStatus();

			// fromStatus에서만 canApply가 true여야 함
			assertThat(eventType.canApply(fromStatus)).isTrue();
			// toStatus가 다르다면 toStatus에서는 canApply가 false여야 함
			if (fromStatus != toStatus) {
				assertThat(eventType.canApply(toStatus)).isFalse();
			}
		}
	}
}