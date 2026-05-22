package app.giftify.auth.application.inbound;

import app.giftify.shared.domain.vo.MemberInfo;

import java.util.Optional;

/**
 * SPA SDK(BFF 패턴)에서 전달받은 idToken을 검증하고
 * 회원 정보와 가입 여부를 반환하는 UseCase.
 */
public interface LoginUseCase {

    LoginResult login(LoginCommand command);

    //-- 레코드 --//

    record LoginCommand(String idToken) {
    }

    /**
     * 로그인 결과
     *
     * @param member    회원 정보 (신규 사용자면 empty)
     * @param isNewUser 신규 사용자 여부 (true: 온보딩 필요)
     * @param authSub   Auth0 고유 식별자
     * @param email     사용자 이메일
     * @param nickname  사용자 이름
     */
    record LoginResult(
            Optional<MemberInfo> member,
            boolean isNewUser,
            String authSub,
            String email,
            String nickname
    ) {
        public static LoginResult existingMember(MemberInfo member) {
            return new LoginResult(
                    Optional.of(member),
                    false,
                    member.authSub(),
                    member.email(),
                    member.nickname()
            );
        }

        public static LoginResult newUser(String authSub, String email, String name) {
            return new LoginResult(
                    Optional.empty(),
                    true,
                    authSub,
                    email,
                    name
            );
        }
    }

}
