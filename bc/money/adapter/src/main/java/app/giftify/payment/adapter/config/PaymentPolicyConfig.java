package app.giftify.payment.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import domain.payment.ChargePaymentPolicy;
import domain.payment.FundingPaymentPolicy;
import domain.payment.PaymentPolicy;

/**
 * 결제 정책(PaymentPolicy) 빈 등록 설정.
 *
 * <p>도메인 모듈(core)은 Spring 의존성을 가지지 않도록 설계되어 있으므로,
 * 어댑터 레이어에서 빈으로 등록합니다.</p>
 */
@Configuration
public class PaymentPolicyConfig {

	@Bean
	public PaymentPolicy chargePaymentPolicy() {
		return new ChargePaymentPolicy();
	}

	@Bean
	public PaymentPolicy fundingPaymentPolicy() {
		return new FundingPaymentPolicy();
	}
}
