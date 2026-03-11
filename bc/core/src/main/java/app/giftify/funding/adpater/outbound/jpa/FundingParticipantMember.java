package app.giftify.funding.adpater.outbound.jpa;

import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.support.jpa.BaseJpaEntity;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_PARTICIPANT_MEMBERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundingParticipantMember extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_id", nullable = false)
    private Funding funding;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private Integer amount;


    public FundingParticipantMember(Funding funding, Long participantId, String nickName, Integer amount) {
        this.funding = funding;
        this.participantId = participantId;
        this.nickName = nickName;
        this.amount = amount;
    }

    public void addAmount(Integer amount) {
        if (amount == null || amount < 1000) {
            throw new FundingException(FundingErrorCode.INVALID_AMOUNT);
        }
        this.amount += amount;
    }

}
