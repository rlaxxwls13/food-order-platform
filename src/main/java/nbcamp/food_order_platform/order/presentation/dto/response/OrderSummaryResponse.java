package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderSummaryResponse(
        UUID orderId,
        String storeName,
        String representativeItemName,
        Long totalAmount,
        OrderStatus orderStatus,
        String statusDescription,
        LocalDateTime createdAt) {
}
