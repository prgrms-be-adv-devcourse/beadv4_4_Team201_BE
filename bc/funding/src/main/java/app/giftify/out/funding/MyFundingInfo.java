package app.giftify.out.funding;

import app.giftify.domain.funding.Funding;

public record MyFundingInfo(
        Funding funding,
        Integer myContribution
) {
    // 생성자에서 null 처리
    public MyFundingInfo(Funding funding, Long myContribution) {
        this(funding, myContribution != null ? myContribution.intValue() : 0);
    }
}
