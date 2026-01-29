package app.giftify.product.adapter.outbound;

import static app.giftify.product.domain.exception.ProductErrorCode.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.QProduct;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.product.adapter.inbound.MyProductSearchDto;
import app.giftify.product.adapter.inbound.ProductSearchDto;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductQueryRepository {
	private final JPAQueryFactory queryFactory;

	// 일반 상품 검색
	@Override
	public Page<Product> searchProducts(ProductSearchDto searchDto) {
		QProduct product = QProduct.product;
		BooleanBuilder where = new BooleanBuilder();

		applyCommonFilter(searchDto, product, where);

		// ACTIVE 상품만
		where.and(product.status.eq(ProductStatus.ACTIVE));

		return fetchPage(searchDto, product, where);
	}

	// 내가 판매하는 상품 조회
	@Override
	public Page<Product> searchMyProducts(Long sellerId, MyProductSearchDto searchDto) {
		QProduct product = QProduct.product;
		BooleanBuilder where = new BooleanBuilder();

		applyCommonFilter(searchDto, product, where);

		// sellerId 고정
		where.and(product.sellerId.eq(sellerId));

		// 상품 상태 필터
		if (searchDto.getStatus() != null) {
			where.and(product.status.eq(searchDto.getStatus()));
		}

		return fetchPage(searchDto, product, where);
	}

	// ---------------------------
	// 일반 검색, 나의 상품 조회 공통 필터
	private void applyCommonFilter(ProductSearchDto searchDto, QProduct product, BooleanBuilder where) {
		String keyword = searchDto.getKeyword();
		Integer minPrice = searchDto.getMinPrice();
		Integer maxPrice = searchDto.getMaxPrice();

		if (keyword != null) {
			keyword = keyword.trim();
			if (!keyword.isBlank()) {
				where.and(
					product.name.containsIgnoreCase(keyword)
						.or(product.description.containsIgnoreCase(keyword))
				);
			}
		}

		if (minPrice != null && maxPrice != null && minPrice > maxPrice)
			throw new ProductException(INVALID_PRODUCT_SEARCH_PRICE_RANGE);

		if (minPrice != null)
			where.and(product.price.goe(searchDto.getMinPrice()));

		if (maxPrice != null)
			where.and(product.price.loe(searchDto.getMaxPrice()));

		if (Boolean.TRUE.equals(searchDto.getInStock()))
			where.and(product.stock.gt(0));
	}

	private Page<Product> fetchPage(ProductSearchDto dto, QProduct product, BooleanBuilder where) {
		int page = dto.getPage();
		int size = dto.getSize();

		List<Product> content = queryFactory
			.selectFrom(product)
			.where(where)
			.orderBy(toOrderSpecifier(dto.getSort(), product))
			.offset((long)page * size)
			.limit(size)
			.fetch();

		Long total = queryFactory
			.select(product.count())
			.from(product)
			.where(where)
			.fetchOne();

		Pageable pageable = PageRequest.of(page, size);
		return new PageImpl<>(content, pageable, total == null ? 0 : total);
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
