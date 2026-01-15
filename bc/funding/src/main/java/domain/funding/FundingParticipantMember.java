package domain.funding;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_PARTICIPANT_MEMBER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundingParticipantMember extends BaseEntity {

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
