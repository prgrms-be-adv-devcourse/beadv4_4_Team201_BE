package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistoryJpa;
import app.giftify.product.adapter.outbound.jpa.entity.QProductStockHistoryJpa;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductStockHistoryRepositoryImpl implements ProductStockHistoryQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductStockHistoryJpa> searchStockHistories(Long sellerId, StockHistorySearchCommand searchCommand) {
        QProductStockHistoryJpa history = QProductStockHistoryJpa.productStockHistoryJpa;
        BooleanBuilder where = new BooleanBuilder();

        // 판매자 ID 필수
        where.and(history.sellerId.eq(sellerId));

        // 상품 ID 필터 (선택)
        if (searchCommand.productId() != null) {
            where.and(history.productId.eq(searchCommand.productId()));
        }

        // 변경 타입 필터 (선택)
        if (searchCommand.changeType() != null) {
            where.and(history.changeType.eq(searchCommand.changeType()));
        }

        // 기간 필터 (선택)
        if (searchCommand.fromDate() != null) {
            where.and(history.createdAt.goe(searchCommand.fromDate().atStartOfDay()));
        }
        if (searchCommand.toDate() != null) {
            where.and(history.createdAt.loe(searchCommand.toDate().atTime(LocalTime.MAX)));
        }

        int page = searchCommand.page();
        int size = searchCommand.size();

        List<ProductStockHistoryJpa> content = queryFactory
                .selectFrom(history)
                .where(where)
                .orderBy(getOrderSpecifiers(searchCommand.sort(), history))
                .offset((long) page * size)
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

    private OrderSpecifier<?>[] getOrderSpecifiers(String sort, QProductStockHistoryJpa history) {
        if ("asc".equalsIgnoreCase(sort)) {
            return new OrderSpecifier[]{history.createdAt.asc(), history.id.asc()};
        }
        // 기본값: 최신순
        return new OrderSpecifier[]{history.createdAt.desc(), history.id.asc()};
    }

}
