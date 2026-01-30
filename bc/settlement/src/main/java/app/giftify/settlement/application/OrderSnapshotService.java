package app.giftify.settlement.application;

import app.giftify.settlement.adapter.outbound.jpa.repository.OrderSnapshotRepository;
import app.giftify.settlement.domain.OrderSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderSnapshotService {
    private final OrderSnapshotRepository orderSnapshotRepository;

    @Transactional
    public OrderSnapshot save(OrderSnapshot snapshot) {
        return orderSnapshotRepository.save(snapshot);
    }

    public OrderSnapshot findById(Long orderId) {
        return orderSnapshotRepository.findById(orderId)
                .orElseThrow(RuntimeException::new);
    }
}
