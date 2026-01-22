package app.giftify.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_MEMBER")
@Getter
@NoArgsConstructor
public class FundingMember {
	@Id
	private Long id;
	private String authSub;
	private String nickname;

	public FundingMember(Long id, String authSub, String nickname) {
		this.id = id;
		this.authSub = authSub;
		this.nickname = nickname;
	}

	public void update(String authSub, String nickname) {
		this.authSub = authSub;
		this.nickname = nickname;
	}
}
