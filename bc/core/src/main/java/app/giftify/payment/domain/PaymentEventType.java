package app.giftify.payment.domain;

/**
 * 결제 이벤트 타입.
 * 각 이벤트는 허용되는 시작 상태(fromStatus)와 결과 상태(toStatus)를 정의합니다.
 */
public enum PaymentEventType {
	CREATED(null, PaymentStatus.PENDING),
	PAID(PaymentStatus.PENDING, PaymentStatus.PAID),
	FAILED(PaymentStatus.PENDING, PaymentStatus.FAILED),
	CANCELED(PaymentStatus.PENDING, PaymentStatus.CANCELED),
	CANCEL_AFTER_PAID(PaymentStatus.PAID, PaymentStatus.CANCELED),
	CANCEL_FAILED(PaymentStatus.PAID, PaymentStatus.PAID);

	private final PaymentStatus fromStatus;
	private final PaymentStatus toStatus;

	PaymentEventType(PaymentStatus fromStatus, PaymentStatus toStatus) {
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
	}

	/**
	 * 주어진 현재 상태에서 이 이벤트를 적용할 수 있는지 확인합니다.
	 *
	 * @param currentStatus 현재 결제 상태
	 * @return 이벤트 적용 가능 여부
	 */
	public boolean canApply(PaymentStatus currentStatus) {
		return fromStatus == currentStatus;
	}

	/**
	 * 이 이벤트가 적용된 후의 결과 상태를 반환합니다.
	 *
	 * @return 결과 상태
	 */
	public PaymentStatus getResultStatus() {
		return toStatus;
	}

	/**
	 * 이 이벤트가 상태 변경을 수반하는지 확인합니다.
	 *
	 * @return 상태 변경 여부
	 */
	public boolean changesState() {
		return fromStatus != toStatus;
	}

	/**
	 * 이 이벤트의 시작 상태를 반환합니다.
	 *
	 * @return 시작 상태 (CREATED의 경우 null)
	 */
	public PaymentStatus getFromStatus() {
		return fromStatus;
	}
}
