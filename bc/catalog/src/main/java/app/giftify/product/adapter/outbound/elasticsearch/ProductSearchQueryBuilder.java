package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.application.port.out.ProductEsSearchCommand;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchQueryBuilder {

    public Query createQuery(ProductEsSearchCommand command) {
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

            // 1순위: ngram 부분 매칭
            boolBuilder.should(s -> s.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("name.ngram")
                    .minimumShouldMatch("75%")
                    .boost(3f)
            ));

            // 2순위: nori 형태소 매칭
            boolBuilder.should(s -> s.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("name", "description")
                    .operator(Operator.And)
                    .boost(2f)
            ));

            // 3순위: jamo 오타 보정
            boolBuilder.should(s -> s.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("name.jamo^2", "description.jamo")
                    .fuzziness("AUTO")
                    .boost(1f)
            ));

            // 4순위: 판매자명 keyword 매칭
            boolBuilder.should(s -> s.fuzzy(fz -> fz
                    .field("sellerNickname")
                    .value(keyword)
                    .fuzziness("AUTO")
                    .boost(0.5f)
            ));

            boolBuilder.minimumShouldMatch("1");
        }

        return Query.of(q -> q.bool(boolBuilder.build()));
    }
}
