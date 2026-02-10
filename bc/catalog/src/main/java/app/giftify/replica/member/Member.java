package app.giftify.replica.member;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEMBER_REPLICA")
@Getter
@NoArgsConstructor
public class Member { // NOTE CatalogMember 등으로 이름 바꾸기
    @Id
    private Long id;
    private String nickname;

    public Member(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public void update(String nickname) {
        this.nickname = nickname;
    }
}
