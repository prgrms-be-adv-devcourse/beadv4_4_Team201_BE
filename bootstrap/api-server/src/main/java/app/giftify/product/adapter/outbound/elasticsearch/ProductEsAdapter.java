package app.giftify.product.adapter.outbound.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.product.readmodel.MemberView;
import app.giftify.product.readmodel.MemberViewRepository;
import app.giftify.support.common.api.paging.PageResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.BulkFailureException;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEsAdapter implements ProductEsPort {
	private static final Logger log = LoggerFactory.getLogger(ProductEsAdapter.class);


    private final ProductEsRepository productEsRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MemberViewRepository memberRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductSearchQueryBuilder queryBuilder;

    @Override
    public void save(Product product) {
        String sellerNickname = memberRepository.findById(product.getSellerId())
                .map(MemberView::getNickname)
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
                .collect(Collectors.toMap(MemberView::getId, MemberView::getNickname));

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

        // 결과가 없으면 바로 반환
        if (searchHits.getTotalHits() == 0) {
            return PageResponse.of(List.of(), command.page(), command.size(), 0);
        }

        // N+1 방지를 위한 ID 벌크 조회
        List<Long> productIds = searchHits.getSearchHits().stream()
                .map(hit -> Long.parseLong(hit.getId()))
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .map(productMapper::toDomain)
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<ProductResult> results = searchHits.getSearchHits().stream()
                .map(hit -> {
                    ProductDocument doc = hit.getContent();
                    Product product = productMap.get(Long.parseLong(doc.getId()));

                    // RDB에 해당 상품이 없는 경우는 결과에서 제외
                    if (product == null) {
                        return null;
                    }

                    return ProductEsMapper.toProductResult(doc, product);
                })
                .filter(java.util.Objects::nonNull) // null인 경우 제외
                .toList();

        long totalElements = searchHits.getTotalHits();

        return PageResponse.of(results, command.page(), command.size(), totalElements);
    }

    @Override
    public void deleteById(Long productId) {
        productEsRepository.deleteById(String.valueOf(productId));
    }

    @Override
    public void updateSellerNickname(Long sellerId, String nickname) {
        List<Long> productIds = productRepository.findActiveProductIdsBySellerId(sellerId);
        if (productIds.isEmpty()) {
            log.info("판매자 닉네임 ES 업데이트 대상 없음: sellerId={}", sellerId);
            return;
        }

        List<UpdateQuery> updateQueries = productIds.stream()
                .map(id -> UpdateQuery.builder(String.valueOf(id))
                        // sellerNickname 필드만 새 값으로 변경 (나머지 필드는 그대로 유지)
                        .withDocument(Document.create().append("sellerNickname", nickname))
                        .build())
                .toList();

        try {
            elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("products"));
            log.info("판매자 닉네임 ES 일괄 업데이트 완료: sellerId={}, nickname={}, updated={}",
                    sellerId, nickname, productIds.size());
        } catch (BulkFailureException e) { // 일부 도큐먼트 누락 시, 예외를 던지지 않고 로그 기록
            int failed = e.getFailedDocuments().size();
            int succeeded = productIds.size() - failed;
            log.warn("판매자 닉네임 ES 일괄 업데이트 부분 성공: sellerId={}, nickname={}, succeeded={}, failed={}, failedIds={}",
                    sellerId, nickname, succeeded, failed, e.getFailedDocuments().keySet());
        } catch (Exception e) { // ES 연결 실패 등 전체 장애
            log.error("판매자 닉네임 ES 업데이트 실패: sellerId={}, nickname={}", sellerId, nickname, e);
            throw new ProductException(ProductErrorCode.ES_NICKNAME_UPDATE_FAILED);
        }
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
