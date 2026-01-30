package app.giftify.settlement.application;

import app.giftify.settlement.adapter.outbound.jpa.repository.OrderItemSnapshotRepository;
import app.giftify.settlement.domain.OrderItemSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemSnapshotService {
    private final OrderItemSnapshotRepository orderItemSnapshotRepository;

    @Transactional
    public OrderItemSnapshot save(OrderItemSnapshot snapshot) {
        return orderItemSnapshotRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public OrderItemSnapshot findByIdFundingId(Long fundingId) {
        return orderItemSnapshotRepository.findById(fundingId)
                .orElseThrow(RuntimeException::new);    // todo: 비즈니스 예외 처리
    }
}
