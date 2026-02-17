package app.giftify.replica;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_MEMBER_REPLICA")
@Getter
@NoArgsConstructor
public class MemberReplica {

    @Id
    private Long id;
    private String authSub;
    private String nickname;

    public MemberReplica(Long id, String authSub, String nickname) {
        this.id = id;
        this.authSub = authSub;
        this.nickname = nickname;
    }

    public void update(String authSub, String nickname) {
        this.authSub = authSub;
        this.nickname = nickname;
    }

}