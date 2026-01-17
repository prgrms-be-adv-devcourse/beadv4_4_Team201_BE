package app.giftify.member.adapter.in.web.requestDto;

public record MemberUpdateRequest(
        String password,
        String nickname,
        String address,
        String phoneNum,
        String name
) {
}
