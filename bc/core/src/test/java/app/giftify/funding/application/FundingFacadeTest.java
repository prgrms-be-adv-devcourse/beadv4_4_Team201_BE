package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.FundingCreateResult;
import app.giftify.funding.adpater.inbound.dto.FundingContributeRequest;
import app.giftify.funding.adpater.inbound.dto.FundingResponseDto;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.FundingStatus;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FundingFacadeTest {

    @InjectMocks
    private FundingFacade fundingFacade;

    @Mock
    private FundingCreateUseCase fundingCreateUseCase;

    @Mock
    private FundingContributeUseCase fundingContributeUseCase;

    @Test
    @DisplayName("contributeFunding 성공: 펀딩 기여")
    void contributeFunding_Success() {
        // given
        Long fundingId = 100L;
        Long participantId = 2L;
        Integer amount = 5000;
        FundingContributeRequest request = new FundingContributeRequest(fundingId, amount);
        List<FundingContributeRequest> requests = List.of(request);

        // when
        fundingFacade.contributeFunding(requests, participantId);

        // then
        verify(fundingContributeUseCase).contribute(requests, participantId);
    }
}
