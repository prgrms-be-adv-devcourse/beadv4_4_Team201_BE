package app.giftify.auth.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import app.giftify.auth.adapter.outbound.client.MemberApiClient;
import app.giftify.auth.adapter.outbound.client.WalletApiClient;
import app.giftify.auth.application.inbound.LoginUseCase;
import app.giftify.shared.domain.vo.MemberInfo;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {
	private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final AuthService authService;
    private final MemberApiClient memberApiClient;
    private final WalletApiClient walletApiClient;

    @Override
    public LoginResult login(LoginCommand command) {
        // 1. idToken 검증 및 클레임 추출
        Jwt jwt = authService.decodeAndValidateToken(command.idToken());

        String authSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        log.info("[LoginService] 토큰 검증 성공. authSub={}, email={}", authSub, email);

        // 2. 회원 조회 (Internal API 호출)
        Optional<MemberInfo> memberOpt = findMemberByAuthSub(authSub);

        if (memberOpt.isPresent()) {
            log.info("[LoginService] 기존 회원 로그인. memberId={}", memberOpt.get().memberId());
            return LoginResult.existingMember(memberOpt.get());
        }

        // 3. 신규 사용자: 회원 및 지갑 동기 생성
        log.info("[LoginService] 신규 사용자 감지. 회원 생성 시작. authSub={}", authSub);
        
        // 3-1. 회원 생성
        var createRequest = new MemberApiClient.CreateMemberRequest(authSub, email, name);
        MemberInfo newMember = memberApiClient.createMember(createRequest);
        log.info("[LoginService] 회원 생성 완료. memberId={}", newMember.memberId());
        
        // 3-2. 지갑 생성
        var walletRequest = new WalletApiClient.CreateWalletRequest(newMember.memberId());
        var walletResult = walletApiClient.createWallet(walletRequest);
        log.info("[LoginService] 지갑 생성 완료. walletId={}", walletResult.walletId());

        return LoginResult.newUser(authSub, email, name);
    }

    private Optional<MemberInfo> findMemberByAuthSub(String authSub) {
        try {
            var response = memberApiClient.getMemberByAuthSub(authSub);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.debug("[LoginService] 회원 조회 실패 (신규 사용자로 처리). authSub={}", authSub);
            return Optional.empty();
        }
    }
}
