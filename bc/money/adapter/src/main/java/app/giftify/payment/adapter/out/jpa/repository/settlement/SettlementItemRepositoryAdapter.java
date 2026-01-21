package app.giftify.payment.adapter.out.jpa.repository.settlement;

import org.springframework.stereotype.Repository;

import app.giftify.payment.adapter.out.jpa.entity.settlement.JpaSettlementItem;
import domain.settlement.SettlementItem;
import domain.settlement.SettlementItemRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SettlementItemRepositoryAdapter implements SettlementItemRepository {

    private final JpaSettlementItemRepository jpaSettlementItemRepository;

    @Override
    public void save(SettlementItem item) {
        if (item.getId() == null) {
            JpaSettlementItem jpaSettlementItem = JpaSettlementItem.builder()
                    .pgFee(item.getPgFee().amount())
                    .orderId(item.getOrderId())
                    .paymentKey(item.getPaymentKey())
                    .type(item.getType())
                    .sellerId(item.getSellerId())
                    .settlementAmount(item.getSettlementAmount().amount())
                    .platformFee(item.getPlatformFee().amount())
                    .settlementDate(item.getSettlementDate())
                    .status(item.getStatus())
                    .totalAmount(item.getTotalAmount().amount())
                    .build();

            jpaSettlementItemRepository.save(jpaSettlementItem);
        } else {
            JpaSettlementItem existingSettlementItem = jpaSettlementItemRepository.findById(item.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Settlement item not found"));

            existingSettlementItem.updateStatus(item.getStatus());
        }
    }
}
