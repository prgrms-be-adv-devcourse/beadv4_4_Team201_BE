package app.giftify.member.core.domain.member;

import app.giftify.shared.domain.base.BaseDomainModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Member extends BaseDomainModel {
    private final String email;
    private final String password; // Auth0 사용 시 비어있거나 더미값
    private String nickname;
    private final LocalDate birthday;
    private MemberRole role;
    private String address;
    private Long phoneNum;
    private final String name;
    private MemberStatus status;
    private final String authSub; // Auth0 연동 키

    @Builder
    public Member(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                  String email, String password, String nickname, LocalDate birthday,
                  MemberRole role, String address, Long phoneNum, String name,
                  MemberStatus status, String authSub) {

        super(id, createdAt, updatedAt);

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
}
