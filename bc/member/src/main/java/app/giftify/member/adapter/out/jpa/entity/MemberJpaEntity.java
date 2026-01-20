package app.giftify.member.adapter.out.jpa.entity;

import app.giftify.member.core.domain.member.MemberRole;
import app.giftify.member.core.domain.member.MemberStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "members")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MemberJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

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
}
