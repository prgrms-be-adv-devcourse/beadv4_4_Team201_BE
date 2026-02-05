/**
 * Order Bounded Context.
 *
 * <p>이 패키지는 더 이상 사용되지 않으며, {@code app.giftify.orderDemo} 패키지로 대체되었습니다.
 *
 * <p>주요 변경 사항:
 * <ul>
 *   <li>REST API 호출 방식에서 Facade 패턴(직접 서비스 호출)으로 전환</li>
 *   <li>PaymentMethod 열거형 통합 (shared.domain.type.PaymentMethod 사용)</li>
 *   <li>idempotencyKey를 orderId로 대체</li>
 * </ul>
 *
 * @deprecated Use {@link app.giftify.orderDemo} instead. This package will be removed in a future release.
 */
@Deprecated(since = "2.0", forRemoval = true)
package app.giftify.order;
