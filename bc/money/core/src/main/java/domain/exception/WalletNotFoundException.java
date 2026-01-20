package domain.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Long walletId) {
        super("Wallet not found. walletId=" + walletId);
    }

    public WalletNotFoundException(String message) {
        super(message);
    }
}
