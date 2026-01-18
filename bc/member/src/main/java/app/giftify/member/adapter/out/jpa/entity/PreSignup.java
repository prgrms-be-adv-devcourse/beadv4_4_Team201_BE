package app.giftify.member.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pre_signup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreSignup {
    @Id
    private String authSub;
    private String email;
    private String name;
    private String nickname;

    public PreSignup(String authSub, String email, String name, String nickname) {
        this.authSub = authSub;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
    }
}
