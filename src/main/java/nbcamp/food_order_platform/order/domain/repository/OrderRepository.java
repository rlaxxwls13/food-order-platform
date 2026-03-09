package nbcamp.food_order_platform.order.domain.repository;

import nbcamp.food_order_platform.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // 특정 유저의 주문 목록 조회
    List<Order> findAllByUserUserId(Long userId);
}
