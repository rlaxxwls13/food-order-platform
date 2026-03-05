package nbcamp.food_order_platform.order.application.dto.result;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderSummaryResult(
        UUID orderId,
        String storeName,
        String representativeItemName,
        Long totalAmount,
        OrderStatus status,
        String statusDescription,
        LocalDateTime createdAt
) {}