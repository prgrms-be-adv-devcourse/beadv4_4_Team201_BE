package app.giftify.payment.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class JpaMoneyMember {
    @Id
    private Long id;

    // todo: JpaMoneyMember 필드 추가

    public JpaMoneyMember() {
    }

    public JpaMoneyMember(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
