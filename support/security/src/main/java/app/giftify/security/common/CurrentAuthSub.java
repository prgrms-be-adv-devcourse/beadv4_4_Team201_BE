package app.giftify.security.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 현재 인증된 사용자의 authSub(Auth0 식별자)를 주입받는 어노테이션.
 *
 * <p>기존 {@code @AuthenticatedMember String authSub} 패턴의 Spring-native 대체.</p>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @GetMapping("/check-registration")
 * public ResponseEntity<?> check(@CurrentAuthSub String authSub) {
 *     // Auth0 식별자로 회원 등록 여부 확인
 * }
 * }</pre>
 *
 * @see MemberPrincipal#authSub()
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "authSub")
public @interface CurrentAuthSub {
}
