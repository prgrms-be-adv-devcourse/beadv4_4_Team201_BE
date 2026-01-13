package app.giftify.payment.adapter.out.jpa.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vo.Money;

import java.time.LocalDateTime;

@Entity
@EntityListeners(value = AuditingEntityListener.class)
public class JpaWallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private JpaMoneyMember member;

    @Embedded
    private Money balance;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    public JpaWallet() {
    }

    public JpaWallet(JpaMoneyMember member) {
        this(member, Money.zero());
    }

    private JpaWallet(JpaMoneyMember member, Money balance) {
        this.member = member;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public JpaMoneyMember getMember() {
        return member;
    }

    public Money getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }
}
