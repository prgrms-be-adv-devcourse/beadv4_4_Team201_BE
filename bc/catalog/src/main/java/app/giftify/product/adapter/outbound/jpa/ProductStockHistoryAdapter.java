package app.giftify.product.adapter.outbound.jpa;

import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistoryJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductStockHistoryRepository;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.domain.ProductStockHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductStockHistoryAdapter implements ProductStockHistoryRepositoryPort {

    private final ProductStockHistoryRepository productStockHistoryRepository;
    private final ProductStockHistoryMapper productStockHistoryMapper;

    @Override
    public ProductStockHistory save(ProductStockHistory productStockHistory) {
        ProductStockHistoryJpa entity = productStockHistoryMapper.toEntity(productStockHistory);
        ProductStockHistoryJpa savedEntity = productStockHistoryRepository.save(entity);
        return productStockHistoryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductStockHistory> findById(Long productStockHistoryId) {
        return productStockHistoryRepository.findById(productStockHistoryId)
                .map(productStockHistoryMapper::toDomain);
    }

    @Override
    public Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchCommand searchCommand) {
        Page<ProductStockHistoryJpa> entityPage = productStockHistoryRepository.searchStockHistories(sellerId, searchCommand);
        return entityPage.map(productStockHistoryMapper::toDomain);
    }

    @Override
    public int deleteByProductIds(List<Long> productIds) {
        return productStockHistoryRepository.deleteByProductIdIn(productIds);
    }
}
