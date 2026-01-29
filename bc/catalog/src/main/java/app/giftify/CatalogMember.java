package app.giftify;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CATALOG_MEMBER")
@Getter
@NoArgsConstructor
public class CatalogMember {
    @Id
    private Long id;
    private String nickname;

    public CatalogMember(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public void update(String nickname) {
        this.nickname = nickname;
    }
}