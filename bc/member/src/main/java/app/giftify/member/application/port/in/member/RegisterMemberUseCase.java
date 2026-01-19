package app.giftify.member.application.port.in.member;

import app.giftify.member.adapter.in.web.requestDto.member.SignupRequest;
import app.giftify.member.core.domain.member.Member;
import jakarta.validation.Valid;

import java.time.LocalDate;

// 사용자가 입력한 추가 정보를 바탕으로 회원가입 처리
public interface RegisterMemberUseCase {

    Member registerMember(RegisterCommand command);

    boolean existsByEmail(String email);

    Member signup(String authSub, @Valid SignupRequest request);

    // 가입 명령을 위한 객체
    // 인증 정보(email, authSub)와 입력 정보(nickname 등) 모두 포함
    record RegisterCommand(
            String email,
            String nickname,
            LocalDate birthday,
            String address,
            String phoneNum,
            String name,
            String authSub
    ) {
    }
}
