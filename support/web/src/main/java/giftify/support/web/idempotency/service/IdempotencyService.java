package giftify.support.web.idempotency.service;

import app.giftify.shared.api.exception.IdempotencyErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.support.jpa.idempotency.IdempotencyHistoryRepository;
import giftify.support.web.idempotency.util.PayloadHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyHistoryRepository repository;
    private final PayloadHasher payloadHasher;

    @Transactional(readOnly = true)
    public void validateIdempotency(String idempotencyKey, Object payload) {
        if (idempotencyKey == null) throw new PolicyException(IdempotencyErrorCode.MISSING_IDEMPOTENCY_KEY);

        repository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(history -> {
                    if (payloadHasher.isMatch(history.getPayloadHash(), payloadHasher.calculateHash(payload))) {
                        throw new PolicyException(IdempotencyErrorCode.DUPLICATE_REQUEST);
                    } else {
                        throw new PolicyException(IdempotencyErrorCode.PAYLOAD_MISMATCH);
                    }
                });
    }
}
