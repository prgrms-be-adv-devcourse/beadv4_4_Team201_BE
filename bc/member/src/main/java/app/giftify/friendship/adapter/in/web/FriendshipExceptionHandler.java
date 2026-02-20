package app.giftify.friendship.adapter.in.web;

import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import app.giftify.friendship.domain.exception.FriendshipException;
import app.giftify.shared.api.response.RsData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(assignableTypes = FriendshipV2Controller.class)
public class FriendshipExceptionHandler {

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
