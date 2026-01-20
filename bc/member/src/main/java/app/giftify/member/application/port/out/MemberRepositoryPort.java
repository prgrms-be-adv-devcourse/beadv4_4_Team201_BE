package app.giftify.member.application.port.out;

import app.giftify.member.core.domain.member.Member;

import java.util.Optional;

// 시스템이 데이터베이스에 회원 정보 요청
public interface MemberRepositoryPort {

    // Auth0 고유 식별자(sub)로 회원 정보 찾기
    Optional<Member> findByAuthSub(String authSub);

    // 내부 ID로 회원 정보 찾기
    Optional<Member> findById(Long id);

    // 이메일로 회원 정보 찾기
    Optional<Member> findByEmail(String email);

    // 회원 정보를 저장 또는 업데이트
    Member save(Member member);

    // 중복하는 닉네임 확인용
    Optional<Member> findByNickname(String nickname);
}
