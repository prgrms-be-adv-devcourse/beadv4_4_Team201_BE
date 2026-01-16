package app.giftify.member.adapter.in.web.dto;

public record MemberUpdateRequest(
        String password,
        String nickname,
        String authSub,
        String address,
        String phoneNum,
        String name
) {
}
