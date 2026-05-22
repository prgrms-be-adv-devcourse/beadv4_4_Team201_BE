package app.giftify.funding.adapter.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.application.FundingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FundingScheduler {
	private static final Logger log = LoggerFactory.getLogger(FundingScheduler.class);

    private final FundingFacade fundingFacade;

    @Scheduled(cron = "0 0 0 * * *")
    public void expireExpiredFundings() {
        LocalDateTime now = LocalDateTime.now();
        List<FundingCompleteResponseDto> expired = fundingFacade.expireExpiredFundings(now);
        log.info("만료 처리된 펀딩 건수: {}", expired.size());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void closeAchievedFundings() {
       List<FundingCompleteResponseDto> achieved = fundingFacade.closeAchievedFundings();
       log.info("목표 달성 펀딩 종료 건수: {}", achieved.size());
    }
}
