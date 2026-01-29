package app.giftify.settlement.application;

import app.giftify.settlement.adapter.outbound.jpa.repository.PaymentSnapshotRepository;
import app.giftify.settlement.domain.PaymentSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentSnapshotService {
    private final PaymentSnapshotRepository paymentSnapshotRepository;

    @Transactional
    public PaymentSnapshot save(PaymentSnapshot snapshot) {
        return paymentSnapshotRepository.save(snapshot);
    }
}
