package app.giftify.security.common.resolver;

import app.giftify.security.common.MemberPrincipal;
import app.giftify.security.common.context.AuthenticatedMember;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @AuthenticatedMember 어노테이션이 붙은 파라미터에 인증 정보를 주입.
 *
 * 지원 타입:
 * - String: authSub
 * - Long: memberId
 * - MemberPrincipal: 전체 인증 정보
 */
public class AuthenticatedMemberArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (!parameter.hasParameterAnnotation(AuthenticatedMember.class)) {
			return false;
		}

		Class<?> type = parameter.getParameterType();
		return type.equals(String.class)
			|| type.equals(Long.class)
			|| type.equals(MemberPrincipal.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		Class<?> paramType = parameter.getParameterType();

		// Case 1: MemberPrincipal (회원가입된 사용자)
		if (principal instanceof MemberPrincipal memberPrincipal) {
			if (paramType.equals(MemberPrincipal.class)) {
				return memberPrincipal;
			}
			if (paramType.equals(Long.class)) {
				return memberPrincipal.memberId();
			}
			if (paramType.equals(String.class)) {
				return memberPrincipal.authSub();
			}
		}

		// Case 2: Jwt (미가입 사용자 또는 필터 미적용) - 하위 호환
		if (principal instanceof Jwt jwt) {
			if (paramType.equals(String.class)) {
				return jwt.getSubject();
			}
			// Long, MemberPrincipal은 미가입자에게 제공 불가
			return null;
		}

		return null;
	}
}
