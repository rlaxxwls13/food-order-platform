package nbcamp.food_order_platform.order.application.dto.result;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResult(
        UUID orderId,
        UUID storeId,
        String storeName,
        OrderStatus status,
        Long totalPrice,
        List<OrderItemInfo> items,
        OrderAddressInfo address,
        LocalDateTime createdAt,
        UserInfo userInfo
) {
    public record UserInfo(UUID userId, String userName) {}
}