package app.giftify.out.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import app.giftify.domain.product.Product;
import app.giftify.domain.product.ProductStatus;
import app.giftify.domain.product.QProduct;
import app.giftify.in.product.ProductSearchDto;
import lombok.RequiredArgsConstructor;


@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductQueryRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Product> search(ProductSearchDto searchDto) {
		QProduct product = QProduct.product;
		BooleanBuilder where = new BooleanBuilder();

		String keyword = searchDto.getKeyword();
		Integer minPrice = searchDto.getMinPrice();
		Integer maxPrice = searchDto.getMaxPrice();
		Boolean inStock = searchDto.getInStock();
		int page = searchDto.getPage();
		int size = searchDto.getSize();

		// 기본 조건: 판매 중인 상품만 조회
		where.and(product.status.eq(ProductStatus.ACTIVE));

		if (keyword != null && !keyword.isBlank()) {
			keyword = keyword.trim(); // 문자열 앞 뒤 공백 제거
			where.and(
				product.name.containsIgnoreCase(keyword)
					.or(product.description.containsIgnoreCase(keyword))
			);
		}
		if (minPrice != null)
			where.and(product.price.goe(minPrice));
		if (maxPrice != null)
			where.and(product.price.loe(maxPrice));

		// 재고 유무 옵션 처리
		if (Boolean.TRUE.equals(inStock))
			where.and(product.stock.gt(0));

		// 데이터 조회
		List<Product> content = queryFactory
			.selectFrom(product)
			.where(where)
			.orderBy(toOrderSpecifier(keyword, product)) // 정렬 조건
			.offset((long)page * size) // 페이지 시작 위치
			.limit(size) // 페이지 크기
			.fetch();

		Long total = queryFactory
			.select(product.count())
			.from(product)
			.where(where)
			.fetchOne();

		long totalElements = (total == null) ? 0 : total;

		Pageable pageable = PageRequest.of(page, size);

		return new PageImpl<>(content, pageable, totalElements);
	}

	private OrderSpecifier<?> toOrderSpecifier(String sort, QProduct product) {
		// 기본 정렬: 생성일 최신순
		if (sort == null || sort.isBlank() || sort.equals("latest"))
			return product.createdAt.desc();

		// 가격 정렬
		return switch (sort) {
			case "priceAsc" -> product.price.asc();
			case "priceDesc" -> product.price.desc();
			default -> product.createdAt.desc();
		};
	}
}
