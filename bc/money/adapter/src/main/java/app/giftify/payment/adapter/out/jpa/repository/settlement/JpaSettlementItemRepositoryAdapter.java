package app.giftify.payment.adapter.out.jpa.repository.settlement;

import app.giftify.payment.adapter.out.jpa.entity.settlement.JpaSettlementItem;
import domain.settlement.SettlementItem;
import domain.settlement.SettlementItemRepository;
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
}
