package giftify.support.web.idempotency.util;

import app.giftify.shared.api.exception.IdempotencyErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import jakarta.servlet.http.HttpServletRequest;

public class HeaderUtil {

    private static final String IDEM_HEADER = "X-Idempotency-Key";

    public static String getIdempotencyKeyOrThrow(HttpServletRequest request) {
        String idempotencyKey = request.getHeader(IDEM_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new PolicyException(IdempotencyErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        return idempotencyKey;
    }
}