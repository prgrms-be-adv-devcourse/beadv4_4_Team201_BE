package app.giftify.member.core.domain.member;

import app.giftify.shared.domain.base.BaseDomainModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

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
                  String email, String password, String nickname, LocalDate birthday,
                  MemberRole role, String address, String phoneNum, String name,
                  MemberStatus status, String authSub) {

        super(id);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.birthday = birthday;
        this.role = role != null ? role : MemberRole.BUYER;
        this.address = address;
        this.phoneNum = phoneNum;
        this.name = name;
        this.status = status != null ? status : MemberStatus.ACTIVE;
        this.authSub = authSub;
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
}
