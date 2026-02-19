package app.giftify.funding.adpater.inbound;

import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.application.FundingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundingScheduler {
    private final FundingFacade fundingFacade;

    @Scheduled(cron = "0 0 0 * * *")
    public void expireExpiredFundings() {
        LocalDateTime now = LocalDateTime.now();
        List<FundingCompleteResponseDto> expired = fundingFacade.expireExpiredFundings(now);
        log.info("만료 처리된 펀딩 건수: {}", expired.size());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void closeAchievedFundings(LocalDateTime now) {
       List<FundingCompleteResponseDto> achieved = fundingFacade.closeAchievedFundings(now);
       log.info("목표 달성 펀딩 종료 건수: {}", achieved.size());
    }
}
