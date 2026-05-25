package app.giftify.security.common;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import app.giftify.security.test.TestMemberFactory;
import app.giftify.support.common.security.MemberRole;
import app.giftify.support.common.security.MemberInfo;

@DisplayName("MemberPrincipal")
class MemberPrincipalTest {

	@Nested
	@DisplayName("생성")
	class Creation {

		@Test
		@DisplayName("MemberInfo로부터 MemberPrincipal을 생성한다")
		void createFromMemberInfo() {
			// given
			MemberInfo memberInfo = TestMemberFactory.createMemberInfo(1L, MemberRole.SELLER);

			// when
			MemberPrincipal principal = MemberPrincipal.from(memberInfo);

			// then
			assertThat(principal).isNotNull();
			assertThat(principal.memberInfo()).isEqualTo(memberInfo);
		}
	}

	@Nested
	@DisplayName("편의 메서드")
	class ConvenienceMethods {

		@Test
		@DisplayName("memberId() 편의 메서드가 동작한다")
		void memberId() {
			// given
			Long expectedMemberId = 123L;
			MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(expectedMemberId, MemberRole.BUYER);

			// when
			Long memberId = principal.memberId();

			// then
			assertThat(memberId).isEqualTo(expectedMemberId);
		}

		@Test
		@DisplayName("authSub() 편의 메서드가 동작한다")
		void authSub() {
			// given
			String expectedAuthSub = "auth0|custom123";
			MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(1L, expectedAuthSub, MemberRole.BUYER);

			// when
			String authSub = principal.authSub();

			// then
			assertThat(authSub).isEqualTo(expectedAuthSub);
		}

		@Test
		@DisplayName("role() 편의 메서드가 동작한다")
		void role() {
			// given
			MemberRole expectedRole = MemberRole.ADMIN;
			MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(1L, expectedRole);

			// when
			MemberRole role = principal.role();

			// then
			assertThat(role).isEqualTo(expectedRole);
		}

		@Test
		@DisplayName("email() 편의 메서드가 동작한다")
		void email() {
			// given
			String expectedEmail = "custom@test.com";
			MemberInfo memberInfo = TestMemberFactory.createMemberInfo(
				1L, "auth0|test", MemberRole.BUYER, expectedEmail, "nick");
			MemberPrincipal principal = MemberPrincipal.from(memberInfo);

			// when
			String email = principal.email();

			// then
			assertThat(email).isEqualTo(expectedEmail);
		}

		@Test
		@DisplayName("nickname() 편의 메서드가 동작한다")
		void nickname() {
			// given
			String expectedNickname = "customNick";
			MemberInfo memberInfo = TestMemberFactory.createMemberInfo(
				1L, "auth0|test", MemberRole.BUYER, "test@test.com", expectedNickname);
			MemberPrincipal principal = MemberPrincipal.from(memberInfo);

			// when
			String nickname = principal.nickname();

			// then
			assertThat(nickname).isEqualTo(expectedNickname);
		}
	}

	@Nested
	@DisplayName("UserDetails 구현")
	class UserDetailsImplementation {

		@Test
		@DisplayName("getAuthorities()가 ROLE_SELLER 형식의 권한을 반환한다")
		void getAuthorities_returnsRolePrefixedAuthority() {
			// given
			MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(1L, MemberRole.SELLER);

			// when
			Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

			// then
			assertThat(authorities).hasSize(1);
			assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_SELLER");
		}

		@Test
		@DisplayName("role이 null일 때 빈 권한 리스트를 반환한다")
		void getAuthorities_returnsEmptyWhenRoleIsNull() {
			// given
			MemberInfo memberInfo = MemberInfo.of(1L, "auth0|test", null, "test@test.com", "nick");
			MemberPrincipal principal = MemberPrincipal.from(memberInfo);

			// when
			Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

			// then
			assertThat(authorities).isEmpty();
		}

		@Test
		@DisplayName("getUsername()이 authSub를 반환한다")
		void getUsername_returnsAuthSub() {
			// given
			String expectedAuthSub = "auth0|username123";
			MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(1L, expectedAuthSub, MemberRole.BUYER);

			// when
			String username = principal.getUsername();

			// then
			assertThat(username).isEqualTo(expectedAuthSub);
		}

		@Test
		@DisplayName("getPassword()가 null을 반환한다 (JWT 기반)")
		void getPassword_returnsNull() {
			// given
			MemberPrincipal principal = TestMemberFactory.createBuyer();

			// when
			String password = principal.getPassword();

			// then
			assertThat(password).isNull();
		}

		@Test
		@DisplayName("계정 상태 메서드들이 항상 true를 반환한다")
		void accountStatusMethods_returnTrue() {
			// given
			MemberPrincipal principal = TestMemberFactory.createBuyer();

			// then
			assertThat(principal.isAccountNonExpired()).isTrue();
			assertThat(principal.isAccountNonLocked()).isTrue();
			assertThat(principal.isCredentialsNonExpired()).isTrue();
			assertThat(principal.isEnabled()).isTrue();
		}
	}

	@Nested
	@DisplayName("역할별 권한")
	class RoleAuthorities {

		@Test
		@DisplayName("BUYER 역할은 ROLE_BUYER 권한을 갖는다")
		void buyerHasRoleBuyer() {
			// given
			MemberPrincipal principal = TestMemberFactory.createBuyer();

			// when
			Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

			// then
			assertThat(authorities).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_BUYER");
		}

		@Test
		@DisplayName("SELLER 역할은 ROLE_SELLER 권한을 갖는다")
		void sellerHasRoleSeller() {
			// given
			MemberPrincipal principal = TestMemberFactory.createSeller();

			// when
			Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

			// then
			assertThat(authorities).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_SELLER");
		}

		@Test
		@DisplayName("ADMIN 역할은 ROLE_ADMIN 권한을 갖는다")
		void adminHasRoleAdmin() {
			// given
			MemberPrincipal principal = TestMemberFactory.createAdmin();

			// when
			Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

			// then
			assertThat(authorities).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_ADMIN");
		}
	}
}
