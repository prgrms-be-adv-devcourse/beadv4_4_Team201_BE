package app.giftify.product.adapter.outbound.dataInit;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.ProductStatus.DRAFT;

@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 데이터가 이미 존재하면 초기화하지 않음
        if (productRepository.count() > 0) {
            return;
        }

        // 초기 상품 데이터 목록 정의
        List<ProductData> products = List.of(
                new ProductData("에어팟 프로 2세대", "애플 정품 노이즈 캔슬링 이어폰", 359000),
                new ProductData("스타벅스 텀블러", "리유저블 콜드컵 710ml 그린", 23000),
                new ProductData("닌텐도 스위치 OLED", "화이트 에디션 새상품", 415000),
                new ProductData("다이슨 에어랩", "컴플리트 롱 니켈/코퍼", 699000),
                new ProductData("레고 스타워즈", "밀레니엄 팔콘 75375", 89000),
                new ProductData("캠핑 감성 랜턴", "충전식 LED 무드등 빈티지 스타일", 32000),
                new ProductData("고양이 자동 급식기", "6L 대용량 스마트 펫 피더", 78000),
                new ProductData("마샬 스피커", "액톤 III 블루투스 스피커 블랙", 489000),
                new ProductData("무지 아로마 디퓨저", "초음파 가습기 겸용 500ml", 45000),
                new ProductData("몽블랑 볼펜", "마이스터스튁 클래식 블랙", 520000)
        );

        // 각 상품 데이터를 ProductJpa 엔티티로 변환하여 저장
        for (int i = 0; i < products.size(); i++) {
            ProductData data = products.get(i);
            // 처음 5개 상품은 ACTIVE 상태, 나머지는 DRAFT 상태로 설정
            ProductStatus status = (i < 5) ? ACTIVE : DRAFT;

            ProductJpa productJpa = ProductJpa.builder()
                    .sellerId(3L)
                    .name(data.name)
                    .description(data.description)
                    .price(data.price)
                    .stock(50)
                    .status(status) // 설정된 상태 직접 지정
                    .build();

            productRepository.save(productJpa);
        }
    }

    private record ProductData(String name, String description, int price) {
    }
}
