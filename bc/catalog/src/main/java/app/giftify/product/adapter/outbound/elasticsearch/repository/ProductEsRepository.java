package app.giftify.product.adapter.outbound.elasticsearch.repository;

import app.giftify.product.adapter.outbound.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, String> {
}
