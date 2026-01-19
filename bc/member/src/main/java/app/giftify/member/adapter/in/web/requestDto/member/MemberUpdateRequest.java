package app.giftify.member.adapter.in.web.requestDto.member;

public record MemberUpdateRequest(
        String password,
        String nickname,
        String address,
        String phoneNum,
        String name
) {
}
