package app.giftify.security.common;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자의 memberId(DB PK)를 주입받는 어노테이션.
 *
 * <h3>사용 시점</h3>
 * <ul>
 *   <li><b>가입된 회원만 호출 가능한 엔드포인트</b>에서 사용</li>
 *   <li>예: 내 정보 조회/수정, 회원 탈퇴, 상품 등록</li>
 *   <li>DB에 회원 레코드가 있어야 값이 존재함</li>
 *   <li>미가입 사용자가 호출하면 null 반환</li>
 * </ul>
 *
 * <h3>vs @CurrentAuthSub</h3>
 * <ul>
 *   <li>{@code @CurrentMemberId}: DB PK (가입자만 값 있음, 미가입자는 null)</li>
 *   <li>{@code @CurrentAuthSub}: Auth0 식별자 (미가입자도 값 있음)</li>
 * </ul>
 *
 * <h3>장점</h3>
 * <ul>
 *   <li>authSub → memberId 변환을 위한 추가 DB 조회 불필요</li>
 *   <li>MemberPrincipalFilter에서 이미 조회된 memberId 재사용</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 가입된 회원 전용 - @CurrentMemberId 사용
 * @GetMapping("/me")
 * public ResponseEntity<?> getMe(@CurrentMemberId Long memberId) {
 *     return memberService.getMemberById(memberId);  // PK로 바로 조회
 * }
 *
 * @PostMapping("/products")
 * @PreAuthorize("hasRole('SELLER')")
 * public ResponseEntity<?> create(@CurrentMemberId Long sellerId, ...) {
 *     // sellerId로 상품 등록
 * }
 * }</pre>
 *
 * @see CurrentAuthSub 미가입 사용자도 호출 가능한 엔드포인트용
 * @see MemberPrincipal#memberId()
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "memberId")
public @interface CurrentMemberId {
}
