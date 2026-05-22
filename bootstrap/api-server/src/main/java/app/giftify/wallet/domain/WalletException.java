package app.giftify.wallet.domain;

import app.giftify.shared.api.exception.DomainException;

public class WalletException extends DomainException {

	public WalletException(WalletErrorCode errorCode) {
		super(errorCode);
	}

	public WalletException(WalletErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public WalletException(WalletErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
