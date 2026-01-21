package app.giftify.security.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.test.context.support.WithSecurityContext;

import app.giftify.shared.domain.type.MemberRole;

/**
 * 테스트에서 MemberPrincipal 기반 인증 컨텍스트를 설정하는 어노테이션.
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Test
 * @WithMockMemberPrincipal(memberId = 1L, role = MemberRole.SELLER)
 * void testWithSeller() {
 *     // SecurityContext에 SELLER 권한의 MemberPrincipal이 설정됨
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMemberPrincipalSecurityContextFactory.class)
public @interface WithMockMemberPrincipal {

	/**
	 * 회원 ID (DB PK)
	 */
	long memberId() default 1L;

	/**
	 * Auth0 식별자 (JWT subject)
	 */
	String authSub() default "auth0|test123";

	/**
	 * 회원 역할
	 */
	MemberRole role() default MemberRole.BUYER;

	/**
	 * 이메일
	 */
	String email() default "test@example.com";

	/**
	 * 닉네임
	 */
	String nickname() default "tester";
}
