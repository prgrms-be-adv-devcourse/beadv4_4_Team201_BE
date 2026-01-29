package app.giftify.funding.out.funding;

import app.giftify.funding.domain.funding.Funding;

public record MyFundingInfo(
        Funding funding,
        Integer myContribution
) {
    // 생성자에서 null 처리
    public MyFundingInfo(Funding funding, Long myContribution) {
        this(funding, myContribution != null ? myContribution.intValue() : 0);
    }
}
