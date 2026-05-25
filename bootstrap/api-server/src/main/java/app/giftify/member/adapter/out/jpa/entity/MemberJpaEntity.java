package app.giftify.member.adapter.out.jpa.entity;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.member.domain.member.MemberStatus;
import app.giftify.support.common.security.MemberRole;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MemberJpaEntity extends BaseJpaEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String nickname;

    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    private String address;

    private String phoneNum;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(unique = true)
    private String authSub;

    @Builder
    private MemberJpaEntity(Long id, String email, String nickname, LocalDate birthday,
                            MemberRole role, String address, String phoneNum,
                            String name, MemberStatus status, String authSub) {
        super(id);
        this.email = email;
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
