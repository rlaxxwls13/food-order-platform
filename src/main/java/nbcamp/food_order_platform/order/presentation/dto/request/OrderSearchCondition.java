package nbcamp.food_order_platform.order.presentation.dto.request;

import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderSearchCondition(
        OrderStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate) {
}
