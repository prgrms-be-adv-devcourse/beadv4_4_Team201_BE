package app.giftify.security.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 현재 인증된 사용자의 memberId를 주입받는 어노테이션.
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @PostMapping("/products")
 * @PreAuthorize("hasRole('SELLER')")
 * public ResponseEntity<?> create(@CurrentMemberId Long memberId, ...) {
 *     // memberId 바로 사용
 * }
 * }</pre>
 *
 * @see MemberPrincipal#memberId()
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "memberId") // TODO 컨트롤러 측에서 sellerId 등으로 받을 수 있게 매핑하기 // noSonar
public @interface CurrentMemberId {
}
