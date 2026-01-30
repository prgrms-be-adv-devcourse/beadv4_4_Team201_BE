package app.giftify.member.adapter.in.web.dto;

import java.time.LocalDate;

import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;

public record MemberResponse(
	Long id,
	String email,
	String nickname,
	LocalDate birthday,
	String address,
	String phoneNum,
	String name,
	MemberStatus status
) {
	public static MemberResponse from(Member member) {
		return new MemberResponse(
			member.getId(),
			member.getEmail(),
			member.getNickname(),
			member.getBirthday(),
			member.getAddress(),
			member.getPhoneNum(),
			member.getName(),
			member.getStatus()
		);
	}
}
