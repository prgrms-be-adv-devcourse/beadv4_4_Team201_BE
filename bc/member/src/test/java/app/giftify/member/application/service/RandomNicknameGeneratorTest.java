package app.giftify.member.application.service;

import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RandomNicknameGeneratorTest {

	@Mock
	private MemberRepositoryPort memberRepositoryPort;

	@InjectMocks
	private RandomNicknameGenerator randomNicknameGenerator;

	@Nested
	@DisplayName("Given 닉네임 생성 요청")
	class Given_NicknameGenerationRequest {

		@Nested
		@DisplayName("When 중복되지 않는 닉네임 생성 시")
		class When_UniqueNicknameGenerated {

			@Test
			@DisplayName("Then 형용사+동물+숫자 형식의 닉네임 반환")
			void Then_ReturnsAdjectiveAnimalNumber() {
				// given
				given(memberRepositoryPort.findByNickname(anyString()))
					.willReturn(Optional.empty());

				// when
				String nickname = randomNicknameGenerator.generate();

				// then
				assertThat(nickname).isNotBlank();
				assertThat(nickname).matches("^[가-힣]+[가-힣]+\\d+$");
				verify(memberRepositoryPort, times(1)).findByNickname(anyString());
			}
		}

		@Nested
		@DisplayName("When 닉네임이 중복될 때")
		class When_NicknameDuplicated {

			@Test
			@DisplayName("Then 최대 시도 후 고유 닉네임 반환")
			void Then_RetriesAndReturnsUniqueNickname() {
				// given: 처음 5번은 중복, 6번째부터 고유
				given(memberRepositoryPort.findByNickname(anyString()))
					.willReturn(Optional.of(Member.builder().build()))
					.willReturn(Optional.of(Member.builder().build()))
					.willReturn(Optional.of(Member.builder().build()))
					.willReturn(Optional.of(Member.builder().build()))
					.willReturn(Optional.of(Member.builder().build()))
					.willReturn(Optional.empty());

				// when
				String nickname = randomNicknameGenerator.generate();

				// then
				assertThat(nickname).isNotBlank();
				verify(memberRepositoryPort, times(6)).findByNickname(anyString());
			}

			@Test
			@DisplayName("Then 최대 시도 초과 시 타임스탬프 기반 fallback 닉네임 반환")
			void Then_ReturnsFallbackWhenMaxAttemptsExceeded() {
				// given: 모든 시도가 중복
				given(memberRepositoryPort.findByNickname(anyString()))
					.willReturn(Optional.of(Member.builder().build()));

				// when
				String nickname = randomNicknameGenerator.generate();

				// then
				assertThat(nickname).isNotBlank();
				assertThat(nickname).matches("^[가-힣]+[가-힣]+\\d+$");
				verify(memberRepositoryPort, times(10)).findByNickname(anyString());
			}
		}
	}
}
