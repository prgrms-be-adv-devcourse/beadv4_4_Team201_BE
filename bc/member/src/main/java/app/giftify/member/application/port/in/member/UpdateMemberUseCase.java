package app.giftify.member.application.port.in.member;

import app.giftify.member.core.domain.member.Member;

public interface UpdateMemberUseCase {
    Member updateMember(UpdateCommand command);

    record UpdateCommand(
            String authSub,
            String password,
            String nickname,
            String address,
            String phoneNum,
            String name
    ) {
    }
}
