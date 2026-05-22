package app.giftify.funding.readmodel;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_MEMBER_VIEWS")
@Getter
@NoArgsConstructor
public class MemberView {

    @Id
    private Long id;

    private String nickname;

    public MemberView(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
