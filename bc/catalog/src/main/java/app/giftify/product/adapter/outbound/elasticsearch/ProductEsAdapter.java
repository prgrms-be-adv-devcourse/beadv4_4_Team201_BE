package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import app.giftify.product.adapter.outbound.jpa.ProductMapper;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductSearchSortType;
import app.giftify.product.domain.ProductStatus;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.paging.PageResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEsAdapter implements ProductEsPort {

    private final ProductEsRepository productEsRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MemberRepository memberRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductSearchQueryBuilder queryBuilder;

    @Override
    public void save(Product product) {
        String sellerNickname = memberRepository.findById(product.getSellerId())
                .map(Member::getNickname)
                .orElse("UNKNOWN");
        ProductDocument document = ProductEsMapper.toDocument(product, sellerNickname);

        productEsRepository.save(document);
    }

    @Override
    public int syncAll() {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE || p.getStatus() == ProductStatus.INACTIVE)
                .map(productMapper::toDomain)
                .toList();

        Map<Long, String> sellerNicknameMap = memberRepository.findAll().stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        List<ProductDocument> documents = products.stream().map(
                product -> {
                    String sellerNickname = sellerNicknameMap.getOrDefault(product.getSellerId(), "UNKNOWN");
                    return ProductEsMapper.toDocument(product, sellerNickname);
                }
        ).toList();

        productEsRepository.saveAll(documents);
        return documents.size();
    }

    /**
     * Elasticsearch 검색 기능 구현
     */
    @Override
    public PageResponse<ProductResult> searchProducts(ProductEsSearchCommand command) {
        // Bool Query로 조합
        Query query = queryBuilder.createQuery(command);

        // sort
        Sort sort = buildSort(command.sort());

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(command.page(), command.size(), sort))
                .build();

        // 실제 검색 실행
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        List<ProductResult> results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(ProductEsMapper::toProductResult)
                .toList();

        long totalElements = searchHits.getTotalHits();

        return PageResponse.of(results, command.page(), command.size(), totalElements);
    }

    private Sort buildSort(ProductSearchSortType sortType) {
        if (sortType == null) {
            return Sort.unsorted();
        }
        return switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case RELEVANCE -> Sort.unsorted();
        };
    }
}
