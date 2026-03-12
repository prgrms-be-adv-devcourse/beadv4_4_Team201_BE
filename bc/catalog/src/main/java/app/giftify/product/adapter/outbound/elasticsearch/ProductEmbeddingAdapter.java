package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingAdapter {

    private final EmbeddingModel embeddingModel;

    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    public List<float[]> embedBatch(List<String> texts) {
        return embeddingModel.embed(texts);
    }

//    public String buildEmbeddingInput(String name, String description) {
//        return name + " " + (description != null ? description : "");
//    }

    public String buildEmbeddingInput(Product product) {
        // 1. 카테고리 정보를 앞에 명시해서 모델에게 힌트를 줌
        // 2. 상품명과 설명을 레이블링해서 전달
        return String.format(
                "카테고리: %s, 상품명: %s, 상세설명: %s",
                product.getCategory().name(), // 예: LIVING
                product.getName(),            // 예: 르 라보 상탈 33
                product.getDescription()      // 예: 우디한 향의 니치 향수...
        );
    }

    public String buildSearchInput(String keyword) {
        return String.format("다음 검색어와 연관된 상품 찾기: %s", keyword);
    }
}
