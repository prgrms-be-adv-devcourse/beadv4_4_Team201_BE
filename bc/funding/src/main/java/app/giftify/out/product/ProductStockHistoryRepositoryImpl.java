package app.giftify.out.product;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import app.giftify.domain.product.ProductStockHistory;
import app.giftify.domain.product.QProductStockHistory;
import app.giftify.in.product.StockHistorySearchDto;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductStockHistoryRepositoryImpl implements ProductStockHistoryQueryRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto) {
		QProductStockHistory history = QProductStockHistory.productStockHistory;
		BooleanBuilder where = new BooleanBuilder();

		// 판매자 ID 필수
		where.and(history.sellerId.eq(sellerId));

		// 상품 ID 필터 (선택)
		if (searchDto.getProductId() != null) {
			where.and(history.productId.eq(searchDto.getProductId()));
		}

		// 변경 타입 필터 (선택)
		if (searchDto.getChangeType() != null) {
			where.and(history.changeType.eq(searchDto.getChangeType()));
		}

		// 기간 필터 (선택)
		if (searchDto.getFromDate() != null) {
			where.and(history.createdAt.goe(searchDto.getFromDate().atStartOfDay()));
		}
		if (searchDto.getToDate() != null) {
			where.and(history.createdAt.loe(searchDto.getToDate().atTime(LocalTime.MAX)));
		}

		int page = searchDto.getPage();
		int size = searchDto.getSize();

		List<ProductStockHistory> content = queryFactory
			.selectFrom(history)
			.where(where)
			.orderBy(history.createdAt.desc())
			.offset((long)page * size)
			.limit(size)
			.fetch();

		Long total = queryFactory
			.select(history.count())
			.from(history)
			.where(where)
			.fetchOne();

		Pageable pageable = PageRequest.of(page, size);
		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}
}
