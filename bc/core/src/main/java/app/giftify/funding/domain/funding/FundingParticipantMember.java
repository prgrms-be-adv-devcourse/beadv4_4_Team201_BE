package app.giftify.funding.domain.funding;

import app.giftify.support.jpa.BaseJpaEntity;

import jakarta.persistence.*;

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
    private Long fundingMemberId;

    @Column(nullable = false)
    private Integer amount;


    public FundingParticipantMember(Funding funding, Long fundingMemberId, Integer amount) {
        if (amount == null || amount < 1000) {
            throw new IllegalArgumentException("참여 금액은 1,000원 이상이어야 합니다.");
        }

        this.funding = funding;
        this.fundingMemberId = fundingMemberId;
        this.amount = amount;
    }

}
