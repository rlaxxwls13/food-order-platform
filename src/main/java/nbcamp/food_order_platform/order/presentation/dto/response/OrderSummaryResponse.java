package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.application.dto.result.OrderSummaryResult;
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

    public static OrderSummaryResponse from(OrderSummaryResult result) {
        if (result == null) return null;
        return OrderSummaryResponse.builder()
                .orderId(result.orderId())
                .storeName(result.storeName())
                .representativeItemName(result.representativeItemName())
                .totalAmount(result.totalAmount())
                .orderStatus(result.status())
                .statusDescription(result.statusDescription())
                .createdAt(result.createdAt())
                .build();
    }
}
