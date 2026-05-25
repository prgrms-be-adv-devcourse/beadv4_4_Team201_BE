package app.giftify.security.common;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import app.giftify.security.test.TestMemberFactory;
import app.giftify.support.common.security.MemberRole;

@DisplayName("MemberAuthenticationToken")
class MemberAuthenticationTokenTest {

	@Nested
	@DisplayName("생성")
	class Creation {

		@Test
		@DisplayName("생성 시 authenticated=true로 설정된다")
		void isAuthenticatedWhenCreated() {
			// given
			MemberPrincipal principal = TestMemberFactory.createBuyer();

			// when
			MemberAuthenticationToken token = new MemberAuthenticationToken(principal);

			// then
			assertThat(token.isAuthenticated()).isTrue();
		}

		@Test
		@DisplayName("생성 시 principal의 authorities를 그대로 가져온다")
		void inheritsAuthoritiesFromPrincipal() {
			// given
			MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(1L, MemberRole.SELLER);

			// when
			MemberAuthenticationToken token = new MemberAuthenticationToken(principal);

			// then
			Collection<GrantedAuthority> authorities = token.getAuthorities();
			assertThat(authorities).hasSize(1);
			assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_SELLER");
		}
	}

	@Nested
	@DisplayName("메서드")
	class Methods {

		@Test
		@DisplayName("getPrincipal()이 MemberPrincipal을 반환한다")
		void getPrincipal_returnsMemberPrincipal() {
			// given
			MemberPrincipal principal = TestMemberFactory.createSeller();
			MemberAuthenticationToken token = new MemberAuthenticationToken(principal);

			// when
			MemberPrincipal result = token.getPrincipal();

			// then
			assertThat(result).isEqualTo(principal);
		}

		@Test
		@DisplayName("getCredentials()가 null을 반환한다")
		void getCredentials_returnsNull() {
			// given
			MemberPrincipal principal = TestMemberFactory.createBuyer();
			MemberAuthenticationToken token = new MemberAuthenticationToken(principal);

			// when
			Object credentials = token.getCredentials();

			// then
			assertThat(credentials).isNull();
		}

		@Test
		@DisplayName("getAuthorities()가 principal의 authorities를 반환한다")
		void getAuthorities_returnsPrincipalAuthorities() {
			// given
			MemberPrincipal principal = TestMemberFactory.createAdmin();
			MemberAuthenticationToken token = new MemberAuthenticationToken(principal);

			// when
			Collection<GrantedAuthority> authorities = token.getAuthorities();

			// then
			assertThat(authorities).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_ADMIN");
		}
	}

	@Nested
	@DisplayName("동등성")
	class Equality {

		@Test
		@DisplayName("같은 principal을 가진 토큰은 동등하다")
		void equalTokensWithSamePrincipal() {
			// given
			MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(1L, MemberRole.BUYER);
			MemberAuthenticationToken token1 = new MemberAuthenticationToken(principal);
			MemberAuthenticationToken token2 = new MemberAuthenticationToken(principal);

			// then
			assertThat(token1).isEqualTo(token2);
			assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
		}

		@Test
		@DisplayName("다른 principal을 가진 토큰은 동등하지 않다")
		void notEqualTokensWithDifferentPrincipal() {
			// given
			MemberPrincipal principal1 = TestMemberFactory.createMemberPrincipal(1L, MemberRole.BUYER);
			MemberPrincipal principal2 = TestMemberFactory.createMemberPrincipal(2L, MemberRole.BUYER);
			MemberAuthenticationToken token1 = new MemberAuthenticationToken(principal1);
			MemberAuthenticationToken token2 = new MemberAuthenticationToken(principal2);

			// then
			assertThat(token1).isNotEqualTo(token2);
		}

		@Test
		@DisplayName("다른 타입의 객체와는 동등하지 않다")
		void notEqualToDifferentType() {
			// given
			MemberPrincipal principal = TestMemberFactory.createBuyer();
			MemberAuthenticationToken token = new MemberAuthenticationToken(principal);

			// then
			assertThat(token).isNotEqualTo("not a token");
			assertThat(token).isNotEqualTo(null);
		}
	}
}
