package app.giftify.payment.adapter.outbound.jpa;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaPayment;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.api.paging.Page;
import app.giftify.shared.api.paging.PageRequest;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRepositoryAdapter 테스트")
class PaymentRepositoryAdapterTest {

	@Mock
	private JpaPaymentRepository jpaPaymentRepository;

	@Mock
	private PaymentMapper paymentMapper;

	@InjectMocks
	private PaymentRepositoryAdapter paymentRepositoryAdapter;

	private Payment createTestPayment(Long id) {
		return Payment.builder()
			.id(id)
			.orderId(1L)
			.orderNumber("ORD-001")
			.memberId(100L)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of(new OrderItemSnapshot(1L, Money.of(10000), 200L)))
			.status(PaymentStatus.PAID)
			.build();
	}

	@Nested
	@DisplayName("save 메서드")
	class SaveTests {

		@Test
		@DisplayName("새 Payment 저장 시 JPA save를 호출한다")
		void save_CallsJpaSave_WhenIdIsNull() {
			// given
			Payment newPayment = createTestPayment(null);
			JpaPayment jpaPayment = mock(JpaPayment.class);
			Payment savedPayment = createTestPayment(1L);

			given(paymentMapper.toEntity(newPayment)).willReturn(jpaPayment);
			given(jpaPaymentRepository.save(jpaPayment)).willReturn(jpaPayment);
			given(paymentMapper.toDomain(jpaPayment)).willReturn(savedPayment);

			// when
			Payment result = paymentRepositoryAdapter.save(newPayment);

			// then
			assertThat(result.getId()).isEqualTo(1L);
			then(jpaPaymentRepository).should().save(jpaPayment);
		}

		@Test
		@DisplayName("기존 Payment 업데이트 시 updateFrom을 호출한다")
		void save_CallsUpdateFrom_WhenIdIsNotNull() {
			// given
			Payment existingPayment = createTestPayment(1L);
			JpaPayment existingJpa = mock(JpaPayment.class);
			JpaPayment updatedJpa = mock(JpaPayment.class);
			given(updatedJpa.getOrderItemsJson()).willReturn("[{\"targetId\":1}]");

			given(jpaPaymentRepository.findById(1L)).willReturn(Optional.of(existingJpa));
			given(paymentMapper.toEntity(existingPayment)).willReturn(updatedJpa);
			given(paymentMapper.toDomain(existingJpa)).willReturn(existingPayment);

			// when
			Payment result = paymentRepositoryAdapter.save(existingPayment);

			// then
			then(existingJpa).should().updateFrom(eq(existingPayment), eq("[{\"targetId\":1}]"));
			assertThat(result).isEqualTo(existingPayment);
		}

		@Test
		@DisplayName("존재하지 않는 id로 업데이트 시 예외를 던진다")
		void save_ThrowsException_WhenIdNotFound() {
			// given
			Payment payment = createTestPayment(999L);
			given(jpaPaymentRepository.findById(999L)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> paymentRepositoryAdapter.save(payment))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("999");
		}
	}

	@Nested
	@DisplayName("findById 메서드")
	class FindByIdTests {

		@Test
		@DisplayName("존재하는 Payment를 도메인으로 변환하여 반환한다")
		void findById_ReturnsDomainPayment_WhenExists() {
			// given
			JpaPayment jpaPayment = mock(JpaPayment.class);
			Payment domainPayment = createTestPayment(1L);
			given(jpaPaymentRepository.findById(1L)).willReturn(Optional.of(jpaPayment));
			given(paymentMapper.toDomain(jpaPayment)).willReturn(domainPayment);

			// when
			Optional<Payment> result = paymentRepositoryAdapter.findById(1L);

			// then
			assertThat(result).isPresent();
			assertThat(result.get().getId()).isEqualTo(1L);
		}
	}

	@Nested
	@DisplayName("findByOrderNumber 메서드")
	class FindByOrderNumberTests {

		@Test
		@DisplayName("주문번호로 조회하여 도메인으로 변환한다")
		void findByOrderNumber_ReturnsDomainPayment() {
			// given
			JpaPayment jpaPayment = mock(JpaPayment.class);
			Payment domainPayment = createTestPayment(1L);
			given(jpaPaymentRepository.findByOrderNumber("ORD-001")).willReturn(Optional.of(jpaPayment));
			given(paymentMapper.toDomain(jpaPayment)).willReturn(domainPayment);

			// when
			Optional<Payment> result = paymentRepositoryAdapter.findByOrderNumber("ORD-001");

			// then
			assertThat(result).isPresent();
			assertThat(result.get().getOrderNumber()).isEqualTo("ORD-001");
		}
	}

	@Nested
	@DisplayName("findByMemberId 메서드")
	class FindByMemberIdTests {

		@Test
		@DisplayName("페이징 결과를 Page<Payment>로 변환한다")
		void findByMemberId_ReturnsPageOfDomainPayments() {
			// given
			JpaPayment jpaPayment = mock(JpaPayment.class);
			Payment domainPayment = createTestPayment(1L);
			org.springframework.data.domain.Page<JpaPayment> springPage =
				new PageImpl<>(List.of(jpaPayment), org.springframework.data.domain.PageRequest.of(0, 10), 1);

			given(jpaPaymentRepository.findByMemberId(eq(100L), any(org.springframework.data.domain.PageRequest.class)))
				.willReturn(springPage);
			given(paymentMapper.toDomain(jpaPayment)).willReturn(domainPayment);

			// when
			Page<Payment> result = paymentRepositoryAdapter.findByMemberId(100L, new PageRequest(0, 10));

			// then
			assertThat(result.content()).hasSize(1);
			assertThat(result.totalElements()).isEqualTo(1);
			assertThat(result.content().get(0).getId()).isEqualTo(1L);
		}
	}
}
