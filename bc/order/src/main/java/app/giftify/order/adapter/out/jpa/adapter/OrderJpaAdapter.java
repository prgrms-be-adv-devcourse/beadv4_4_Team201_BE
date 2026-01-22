package app.giftify.order.adapter.out.jpa.adapter;

import app.giftify.order.adapter.out.jpa.entity.OrderEntity;
import app.giftify.order.adapter.out.jpa.mapper.OrderMapper;
import app.giftify.order.adapter.out.jpa.repository.OrderJpaRepository;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 주문 데이터를 DB에 영속화하기 위한 JPA 어댑터
@Repository
@RequiredArgsConstructor
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;

    // 주문 저장 또는 업데이트
    @Override
    public Order save(Order order) {
        OrderEntity entity;
        if (order.getId() != null) {
            entity = orderJpaRepository.findById(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException("수정할 주문을 찾을 수 없습니다."));
            updateEntity(entity, order);
        } else {
            entity = orderMapper.toOrderEntity(order);
        }
        
        OrderEntity savedEntity = orderJpaRepository.save(entity);
        return orderMapper.toOrderDomain(savedEntity);
    }

    // 도메인 객체의 변경사항을 엔티티에 반영
    private void updateEntity(OrderEntity entity, Order order) {
        entity.setStatus(order.getStatus());
        entity.setPaymentKey(order.getPaymentKey());
        entity.setConfirmedAt(order.getConfirmedAt());
        entity.setCancelledAt(order.getCancelledAt());
        
        // 하위 아이템들의 상태도 업데이트 (JPA 영속성 컨텍스트에 의해 관리됨)
        entity.getOrderItems().forEach(itemEntity -> {
            order.getOrderItems().stream()
                    .filter(itemDomain -> itemDomain.getId() != null && itemDomain.getId().equals(itemEntity.getId()))
                    .findFirst()
                    .ifPresent(itemDomain -> {
                        itemEntity.setStatus(itemDomain.getStatus());
                        itemEntity.setConfirmedAt(itemDomain.getConfirmedAt());
                        itemEntity.setCancelledAt(itemDomain.getCancelledAt());
                    });
        });
    }

    // 비관적 락을 사용하여 주문 조회
    @Override
    public Optional<Order> findByIdWithLock(Long orderId) {
        return orderJpaRepository.findByIdWithLock(orderId)
                .map(orderMapper::toOrderDomain);
    }

    // 주문 ID로 조회
    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId)
                .map(orderMapper::toOrderDomain);
    }

    // 결제 대기 상태이며 특정 시간보다 오래된 주문 목록 조회
    @Override
    public List<Order> findPaymentPendingOrdersOlderThan(long minutes) {
        LocalDateTime time = LocalDateTime.now().minusMinutes(minutes);
        return orderJpaRepository.findAllPaymentPendingOlderThan(time).stream()
                .map(orderMapper::toOrderDomain)
                .collect(Collectors.toList());
    }

    // 특정 펀딩 ID를 포함하는 모든 주문 목록 조회
    @Override
    public List<Order> findAllByFundingId(Long fundingId) {
        return orderJpaRepository.findAllByFundingId(fundingId).stream()
                .map(orderMapper::toOrderDomain)
                .collect(Collectors.toList());
    }
}
