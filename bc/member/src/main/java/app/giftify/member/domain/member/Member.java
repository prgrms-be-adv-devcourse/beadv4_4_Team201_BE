package app.giftify.member.domain.member;

import java.time.LocalDate;

import app.giftify.member.domain.exception.MemberStatusException;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.MemberRole;

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

	public static Builder builder() {
		return new Builder();
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
		validate(email, authSub);

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

	@Deprecated
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

	private static void validate(String email, String authSub) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("email은 필수입니다.");
		}
		if (authSub == null || authSub.isBlank()) {
			throw new IllegalArgumentException("authSub는 필수입니다.");
		}
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

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getNickname() {
		return nickname;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public MemberRole getRole() {
		return role;
	}

	public String getAddress() {
		return address;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public String getName() {
		return name;
	}

	public MemberStatus getStatus() {
		return status;
	}

	public String getAuthSub() {
		return authSub;
	}

	public static class Builder {
		private Long id;
		private String email;
		private String nickname;
		private LocalDate birthday;
		private MemberRole role;
		private String address;
		private String phoneNum;
		private String name;
		private MemberStatus status;
		private String authSub;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder nickname(String nickname) {
			this.nickname = nickname;
			return this;
		}

		public Builder birthday(LocalDate birthday) {
			this.birthday = birthday;
			return this;
		}

		public Builder role(MemberRole role) {
			this.role = role;
			return this;
		}

		public Builder address(String address) {
			this.address = address;
			return this;
		}

		public Builder phoneNum(String phoneNum) {
			this.phoneNum = phoneNum;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder status(MemberStatus status) {
			this.status = status;
			return this;
		}

		public Builder authSub(String authSub) {
			this.authSub = authSub;
			return this;
		}

		public Member build() {
			return new Member(id, email, nickname, birthday, role, address, phoneNum, name, status, authSub);
		}
	}
}
