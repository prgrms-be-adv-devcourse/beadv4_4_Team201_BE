package app.giftify.member.core.domain.member;

import app.giftify.shared.domain.base.BaseDomainModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Member 도메인 엔티티
 * 헥사고날 아키텍처 원칙에 따라 순수 자바 객체로 구성하며,
 * 공유 모듈의 BaseDomainModel을 상속받아 식별자와 생성/수정일을 관리합니다.
 */
@Getter
public class Member extends BaseDomainModel {
    private final String email;
    private final String password; // Auth0 사용 시 비어있거나 더미값
    private final String nickname;
    private final LocalDate birthday;
    private final MemberRole role;
    private final String address;
    private final Long phoneNum;
    private final String name;
    private final MemberStatus status;
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
        this.role = role;
        this.address = address;
        this.phoneNum = phoneNum;
        this.name = name;
        this.status = status;
        this.authSub = authSub;
    }
}
