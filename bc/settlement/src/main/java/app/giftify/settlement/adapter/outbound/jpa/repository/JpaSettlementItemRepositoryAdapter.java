package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.adapter.outbound.jpa.entity.JpaSettlementItem;
import app.giftify.settlement.domain.SettlementItem;
import app.giftify.settlement.domain.SettlementItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaSettlementItemRepositoryAdapter implements SettlementItemRepository {

    private final JpaSettlementItemRepository jpaSettlementItemRepository;

    @Override
    public void save(SettlementItem item) {
        if (item.getId() == null) {
            JpaSettlementItem jpaSettlementItem = JpaSettlementItem.from(item);

            jpaSettlementItemRepository.save(jpaSettlementItem);
        } else {
            // todo: 정산 아이템 업데이트 로직 구현
        }
    }

    @Override
    public boolean existsByOrderItemId(Long orderItemId) {
        return jpaSettlementItemRepository.existsByOrderItemId(orderItemId);
    }

    @Override
    public void saveAndFlush(SettlementItem item) {
        if (item.getId() == null) {
            JpaSettlementItem jpaSettlementItem = JpaSettlementItem.from(item);
            jpaSettlementItemRepository.saveAndFlush(jpaSettlementItem);
        } else {
            // todo: 정산 아이템 업데이트 로직 구현
            jpaSettlementItemRepository.flush(); // 업데이트 후 강제 플러시
        }
    }
}
