package app.giftify.member.application.port.in;

import app.giftify.member.core.domain.member.Member;

import java.util.Optional;

// 외부에서 시스템에 회원의 존재 여부 및 상세 정보를 요청(조회)
public interface GetMemberUseCase {

    // Auth0에서 제공한 고유 식별자(sub)를 통해 회원 정보 조회
    // authSub: Auth0 고유 식별자
    // return: 가입된 회원 정보 (없을 경우 Optional.empty())
    Optional<Member> getMemberByAuthSub(String authSub);

    // 내부 관리용 ID를 통해 회원 정보 조회
    Optional<Member> getMemberById(Long id);
}
