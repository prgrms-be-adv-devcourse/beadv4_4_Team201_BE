package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.jpa.ProductMapper;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 배포용 -> init 프로필로 어플리케이션을 실행하여 기초 데이터 동기화 또는 관리자 API
 * - ES에 한 번 색인된 데이터는 ES 볼륨이 살아있는 한 그대로 유지
 * - init 실행 후 init 프로필을 빼고 재시작해도 ES 데이터에는 영향없음
 */
@Profile({"local", "dev", "init"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEsDataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductEsPort productEsPort;

    @Override
    public void run(ApplicationArguments args) {
        List<ProductJpa> products = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE || p.getStatus() == ProductStatus.INACTIVE)
                .toList();

        for (ProductJpa jpa : products) {
            Product product = productMapper.toDomain(jpa);
            productEsPort.save(product);
        }

        log.info("ES 초기 데이터 동기화 완료: ACTIVE 상품 {}건", products.size());
    }
}