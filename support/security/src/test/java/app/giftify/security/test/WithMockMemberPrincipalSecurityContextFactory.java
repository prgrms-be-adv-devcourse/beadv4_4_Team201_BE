package app.giftify.security.test;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.support.common.security.MemberInfo;

/**
 * @WithMockMemberPrincipal 어노테이션을 처리하여 SecurityContext를 생성하는 팩토리.
 */
public class WithMockMemberPrincipalSecurityContextFactory
	implements WithSecurityContextFactory<WithMockMemberPrincipal> {

	@Override
	public SecurityContext createSecurityContext(WithMockMemberPrincipal annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		MemberInfo memberInfo = MemberInfo.of(
			annotation.memberId(),
			annotation.authSub(),
			annotation.role(),
			annotation.email(),
			annotation.nickname()
		);

		MemberPrincipal principal = MemberPrincipal.from(memberInfo);
		MemberAuthenticationToken authentication = new MemberAuthenticationToken(principal);

		context.setAuthentication(authentication);
		return context;
	}
}
