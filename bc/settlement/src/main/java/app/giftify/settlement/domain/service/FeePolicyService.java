package app.giftify.settlement.domain.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FeePolicyService {

    public BigDecimal getPlatformFeeRate() {
        return new BigDecimal("0.01");
    }

    public BigDecimal getPgFeeRate() {
        // todo: paymentMethodType 별 다르게
        return new BigDecimal("0.02");
    }
}
