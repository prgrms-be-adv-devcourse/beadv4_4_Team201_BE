package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import app.giftify.product.adapter.outbound.jpa.ProductMapper;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductEsAdapter implements ProductEsPort {

    private final ProductEsRepository productEsRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDocument save(Product product) {
        ProductDocument document = ProductEsMapper.toDocument(product);
        return productEsRepository.save(document);
    }

    @Override
    public int syncAll() {
        List<ProductDocument> documents = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE || p.getStatus() == ProductStatus.INACTIVE)
                .map(productMapper::toDomain)
                .map(ProductEsMapper::toDocument)
                .toList();

        productEsRepository.saveAll(documents);
        return documents.size();
    }
}
