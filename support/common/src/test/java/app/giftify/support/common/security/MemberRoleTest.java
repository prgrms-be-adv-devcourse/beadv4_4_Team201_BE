package app.giftify.support.common.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("MemberRole 열거형")
class MemberRoleTest {

	@Test
	@DisplayName("모든 역할(BUYER, SELLER, ADMIN)이 정의되어 있다")
	void allRolesAreDefined() {
		// given
		MemberRole[] roles = MemberRole.values();

		// then
		assertThat(roles).hasSize(3);
		assertThat(roles).containsExactlyInAnyOrder(
			MemberRole.BUYER,
			MemberRole.SELLER,
			MemberRole.ADMIN
		);
	}

	@ParameterizedTest(name = "{0} 역할의 설명은 \"{1}\"이다")
	@CsvSource({
		"BUYER, 구매자",
		"SELLER, 판매자",
		"ADMIN, 관리자"
	})
	@DisplayName("각 역할의 한글 설명을 반환한다")
	void roleDescriptions(MemberRole role, String expectedDescription) {
		// when
		String description = role.getDescription();

		// then
		assertThat(description).isEqualTo(expectedDescription);
	}

	@Test
	@DisplayName("역할 이름으로 열거형을 조회할 수 있다")
	void canLookupByName() {
		// when & then
		assertThat(MemberRole.valueOf("BUYER")).isEqualTo(MemberRole.BUYER);
		assertThat(MemberRole.valueOf("SELLER")).isEqualTo(MemberRole.SELLER);
		assertThat(MemberRole.valueOf("ADMIN")).isEqualTo(MemberRole.ADMIN);
	}
}
