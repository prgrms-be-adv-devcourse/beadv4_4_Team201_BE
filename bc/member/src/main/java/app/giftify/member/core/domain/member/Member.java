package app.giftify.member.core.domain.member;

import java.time.LocalDate;

import app.giftify.member.core.domain.exception.MemberStatusException;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Member extends BaseDomainModel {
	private final String email;
	private String password; // Auth0 사용 시 비어있거나 더미값
	private String nickname;
	private final LocalDate birthday;
	private MemberRole role;
	private String address;
	private String phoneNum;
	private String name;
	private MemberStatus status;
	private final String authSub; // Auth0 연동 키

	@Builder
	public Member(Long id,
		String email, String nickname, LocalDate birthday,
		MemberRole role, String address, String phoneNum, String name,
		MemberStatus status, String authSub) {

		super(id);
		this.email = email;
		this.nickname = nickname;
		this.birthday = birthday;
		this.role = role != null ? role : MemberRole.BUYER;
		this.address = address;
		this.phoneNum = phoneNum;
		this.name = name;
		this.status = status != null ? status : MemberStatus.ACTIVE;
		this.authSub = authSub;
	}

	public static Member create(
		String email,
		String nickname,
		LocalDate birthday,
		String address,
		String phoneNum,
		String name,
		String authSub
	) {
		validate(email, nickname, authSub);

		return Member.builder()
			.email(email)
			.nickname(nickname)
			.birthday(birthday)
			.address(address)
			.phoneNum(phoneNum)
			.name(name)
			.authSub(authSub)
			.role(MemberRole.BUYER)
			.status(MemberStatus.ACTIVE)
			.build();
	}

	public void updateInfo(String nickname, String password, String address, String phoneNum, String name) {
		if (password != null) {
			this.password = password;
		}
		if (nickname != null && !nickname.isBlank()) {
			this.nickname = nickname;
		}
		if (address != null && !address.isBlank()) {
			this.address = address;
		}
		if (phoneNum != null) {
			this.phoneNum = phoneNum;
		}
		if (name != null) {
			this.name = name;
		}
	}

	public void withdraw() {
		this.status = MemberStatus.WITHDRAWN;
	}

	private static void validate(String email, String nickname, String authSub) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("email은 필수입니다.");
		}
		if (nickname == null || nickname.isBlank()) {
			throw new IllegalArgumentException("nickname은 필수입니다.");
		}
		if (authSub == null || authSub.isBlank()) {
			throw new IllegalArgumentException("authSub는 필수입니다.");
		}
	}

	// 회원이 활성 상태가 아닐 경우 예외를 던집니다. (검증 및 흐름 제어용)
	public void validateActiveStatus() {
		if (this.status != MemberStatus.ACTIVE) {
			throw new MemberStatusException();
		}
	}

	// 현재 회원의 활성화 상태 여부를 boolean으로 반환합니다. (상태 체크용)
	public boolean isActive() {
		return this.status == MemberStatus.ACTIVE;
	}
}
