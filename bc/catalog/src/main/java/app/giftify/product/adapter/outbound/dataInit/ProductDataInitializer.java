package app.giftify.product.adapter.outbound.dataInit;

import app.giftify.product.adapter.outbound.jpa.entity.Product;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }

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

        for (int i = 0; i < products.size(); i++) {
            ProductData data = products.get(i);

            Product product = Product.builder()
                    .sellerId(3L)
                    .name(data.name)
                    .description(data.description)
                    .price(data.price)
                    .stock(50)
                    .build();

            // 먼저 저장 (DRAFT 상태)
            productRepository.save(product);

            // 5개만 ACTIVE로 상태 변경
            if (i < 5) {
                product.approve();  // DRAFT → INACTIVE
                product.active();   // INACTIVE → ACTIVE
                productRepository.save(product);
            }
        }
    }

    private record ProductData(String name, String description, int price) {
    }
}
