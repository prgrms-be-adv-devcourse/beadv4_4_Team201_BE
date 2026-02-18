package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.inbound.web.ProductSearchSortType;
import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import app.giftify.product.adapter.outbound.jpa.ProductMapper;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.paging.PageResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
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
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // ----- filter 절
        // 항상 판매 중인 상품만 조회
        boolBuilder.filter(f -> f.term(t -> t.field("status").value("ACTIVE")));

        if (command.category() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("category").value(command.category().name())));
        }

        if (command.minPrice() != null || command.maxPrice() != null) {
            boolBuilder.filter(f -> f.range(r -> {
                var numberRange = r.number(n -> {
                    n.field("price");
                    if (command.minPrice() != null) {
                        n.gte(command.minPrice().doubleValue());
                    }
                    if (command.maxPrice() != null) {
                        n.lte(command.maxPrice().doubleValue());
                    }
                    return n;
                });
                return r;
            }));
        }

        // ----- should 절
        // 키워드가 있으면 스코어 기반 검색 수행
        if (command.keyword() != null && !command.keyword().isBlank()) {
            String keyword = command.keyword();
            String keywordNoSpaces = keyword.replaceAll("\\s+", "");

            // 1순위: 원본 검색어 ngram 매칭
            boolBuilder.should(s -> s.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("name.ngram^3", "description.ngram^2", "sellerNickname.ngram")
                    .minimumShouldMatch("70%")
                    .boost(3f)
            ));

            // 공백 제거 검색어가 원본과 다를 경우 추가 매칭
            if (!keywordNoSpaces.equals(keyword)) {
                boolBuilder.should(s -> s.multiMatch(mm -> mm
                        .query(keywordNoSpaces)
                        .fields("name.ngram^3", "description.ngram^2", "sellerNickname.ngram")
                        .minimumShouldMatch("70%")
                        .boost(3f)
                ));
            }

            // 2순위: 자모 분해(NFD) + 오타 허용 (음소 단위 매칭)
            boolBuilder.should(s -> s.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("name.jamo^2", "description.jamo", "sellerNickname.jamo")
                    .fuzziness("AUTO")
                    .boost(1f)
            ));

            if (!keywordNoSpaces.equals(keyword)) {
                boolBuilder.should(s -> s.multiMatch(mm -> mm
                        .query(keywordNoSpaces)
                        .fields("name.jamo^2", "description.jamo", "sellerNickname.jamo")
                        .fuzziness("AUTO")
                        .boost(1f)
                ));
            }

            boolBuilder.minimumShouldMatch("1");
        }

        // Bool Query로 조합
        Query query = Query.of(q -> q.bool(boolBuilder.build()));

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
