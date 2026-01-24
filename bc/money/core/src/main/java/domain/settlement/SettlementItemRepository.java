package domain.settlement;

public interface SettlementItemRepository {
    void save(SettlementItem item);                 // 업데이트용

    void saveAndFlush(SettlementItem item);         // 생성/방어용

    boolean existsByOrderItemId(Long orderItemId);

}
