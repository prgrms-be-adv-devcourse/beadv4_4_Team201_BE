package app.giftify.wallet.adapter.inbound.web.exception;

import app.giftify.support.common.api.response.ErrorResponse;
import app.giftify.wallet.domain.WalletException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.giftify.wallet")
public class WalletExceptionHandler {

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<ErrorResponse> handleWalletException(WalletException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(new ErrorResponse(e.getErrorCode().getCode(), e.getMessage()));
    }
}
