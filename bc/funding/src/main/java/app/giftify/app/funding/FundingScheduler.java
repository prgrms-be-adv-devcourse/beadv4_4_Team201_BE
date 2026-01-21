package app.giftify.app.funding;

import app.giftify.in.funding.FundingCompleteResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundingScheduler {
    private final FundingFacade fundingFacade;

    @Scheduled(cron = "0 0 0 * * *")
    public void expireExpiredFundings() {
        List<FundingCompleteResponseDto> expired = fundingFacade.expireExpiredFundings();
        log.info("만료 처리된 펀딩 건수: {}", expired.size());
    }

//    @Scheduled(cron = "0 0 0 * * *")
//    public void closeAchievedFundings() {
//       List<FundingCompleteResponseDto> achieved = fundingFacade.closeAchievedFundings();
//       log.info("목표 달성 펀딩 종료 건수: {}", achieved.size());
//    }
}
