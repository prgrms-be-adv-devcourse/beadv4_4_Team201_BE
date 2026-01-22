package app.giftify.member.application.port.in;

import app.giftify.member.domain.member.Member;

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
