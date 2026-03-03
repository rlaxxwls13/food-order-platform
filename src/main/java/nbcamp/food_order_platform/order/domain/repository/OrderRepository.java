package nbcamp.food_order_platform.order.domain.repository;


import nbcamp.food_order_platform.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
