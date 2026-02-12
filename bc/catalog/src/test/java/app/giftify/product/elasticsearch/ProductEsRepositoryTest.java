package app.giftify.product.elasticsearch;

import app.giftify.product.ProductTestApplication;
import app.giftify.product.TestContainersConfiguration;
import app.giftify.product.adapter.outbound.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductTestApplication.class)
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
class ProductEsRepositoryTest {

    @Autowired
    private ProductEsRepository productEsRepository;

    @AfterEach
    void tearDown() {
        productEsRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 문서를 저장할 수 있다")
    void create() {
        // given
        ProductDocument document = new ProductDocument("테스트 상품", "테스트 설명", 10000, "ELECTRONICS");

        // when
        ProductDocument saved = productEsRepository.save(document);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트 상품");
        assertThat(saved.getPrice()).isEqualTo(10000);
    }

    @Test
    @DisplayName("상품 문서를 조회할 수 있다")
    void read() {
        // given
        ProductDocument document = new ProductDocument("테스트 상품", "테스트 설명", 10000, "ELECTRONICS");
        ProductDocument saved = productEsRepository.save(document);

        // when
        ProductDocument found = productEsRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getName()).isEqualTo("테스트 상품");
        assertThat(found.getDescription()).isEqualTo("테스트 설명");
        assertThat(found.getPrice()).isEqualTo(10000);
        assertThat(found.getCategory()).isEqualTo("ELECTRONICS");
    }

    @Test
    @DisplayName("상품 문서를 수정할 수 있다")
    void update() {
        // given
        ProductDocument document = new ProductDocument("테스트 상품", "테스트 설명", 10000, "ELECTRONICS");
        ProductDocument saved = productEsRepository.save(document);

        // when
        ProductDocument updated = new ProductDocument(saved.getId(), "수정된 상품", "수정된 설명", 20000, "BEAUTY");
        productEsRepository.save(updated);

        // then
        ProductDocument found = productEsRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("수정된 상품");
        assertThat(found.getDescription()).isEqualTo("수정된 설명");
        assertThat(found.getPrice()).isEqualTo(20000);
        assertThat(found.getCategory()).isEqualTo("BEAUTY");
    }

    @Test
    @DisplayName("상품 문서를 삭제할 수 있다")
    void delete() {
        // given
        ProductDocument document = new ProductDocument("테스트 상품", "테스트 설명", 10000, "ELECTRONICS");
        ProductDocument saved = productEsRepository.save(document);

        // when
        productEsRepository.deleteById(saved.getId());

        // then
        assertThat(productEsRepository.findById(saved.getId())).isEmpty();
    }
}
