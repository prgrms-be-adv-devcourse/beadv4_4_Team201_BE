package app.giftify.product.application.port.in;

import app.giftify.product.domain.ProductCategory;

// UseCase의 입력 값으로 사용될 Command 객체
// record를 사용하여 불변 데이터 객체로 정의
public record ProductCreateCommand(
        String name,
        String description,
        int price,
        int stock,
        ProductCategory category,
        String imageKey
) {
}
