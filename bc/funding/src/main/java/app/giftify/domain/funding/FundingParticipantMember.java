package app.giftify.domain.funding;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_PARTICIPANT_MEMBER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundingParticipantMember extends BaseJpaEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "funding_id", nullable = false)
	private Funding funding;

	@Column(nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private Integer amount;

	public FundingParticipantMember(Funding funding, Long memberId, Integer amount) {
		if (amount == null || amount < 1000) {
			throw new IllegalArgumentException("참여 금액은 1,000원 이상이어야 합니다.");
		}

		this.funding = funding;
		this.memberId = memberId;
		this.amount = amount;
	}

}
