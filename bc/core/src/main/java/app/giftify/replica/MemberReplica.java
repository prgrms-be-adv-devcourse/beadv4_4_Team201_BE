package app.giftify.replica;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CORE_MEMBER_REPLICAS")
@Getter
@NoArgsConstructor
public class MemberReplica {

    @Id
    private Long id;
    private String nickname;

    public MemberReplica(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public void update(String nickname) {
        this.nickname = nickname;
    }

}