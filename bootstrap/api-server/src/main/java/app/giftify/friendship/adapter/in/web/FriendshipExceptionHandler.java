package app.giftify.friendship.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import app.giftify.friendship.domain.exception.FriendshipException;
import app.giftify.support.common.api.response.RsData;
@RestControllerAdvice(assignableTypes = FriendshipV2Controller.class)
public class FriendshipExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(FriendshipExceptionHandler.class);


    @ExceptionHandler(FriendshipException.class)
    public ResponseEntity<RsData<Void>> handleFriendshipException(FriendshipException e) {
        log.error("[Friendship Exception] Code: {}, Message: {}",
                e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatusCode())
                .body(RsData.fail(e.getMessage(), e.getErrorCode().getCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("[Friendship Validation Error] {}", errorMessage);
        return ResponseEntity.status(400)
                .body(RsData.fail(errorMessage, "VALIDATION_ERROR"));
    }
}
