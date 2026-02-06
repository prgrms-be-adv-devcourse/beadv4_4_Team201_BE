package app.giftify.security.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 현재 인증된 사용자의 authSub(Auth0 식별자)를 주입받는 어노테이션.
 *
 * <h3>사용 시점</h3>
 * <ul>
 *   <li><b>미가입 사용자도 호출 가능한 엔드포인트</b>에서 사용</li>
 *   <li>예: 회원가입, 가입 여부 확인</li>
 *   <li>Auth0 인증만 되면 값이 존재함 (DB 회원 레코드 불필요)</li>
 * </ul>
 *
 * <h3>vs @CurrentMemberId</h3>
 * <ul>
 *   <li>{@code @CurrentAuthSub}: Auth0 식별자 (미가입자도 값 있음)</li>
 *   <li>{@code @CurrentMemberId}: DB PK (가입자만 값 있음, 미가입자는 null)</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 미가입 사용자도 호출 가능 - @CurrentAuthSub 사용
 * @PostMapping("/signup")
 * public ResponseEntity<?> signup(@CurrentAuthSub String authSub, ...) {
 *     // authSub로 신규 회원 생성
 * }
 *
 * @GetMapping("/check-registration")
 * public ResponseEntity<?> check(@CurrentAuthSub String authSub) {
 *     // authSub로 가입 여부 확인
 * }
 * }</pre>
 *
 * @see CurrentMemberId 가입된 회원 전용 엔드포인트용
 * @see MemberPrincipal#authSub()
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "authSub")
public @interface CurrentAuthSub {
}
