package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
                UUID orderId,
                UUID storeId,
                String storeName,
                OrderStatus orderStatus,
                Long totalAmount,
                List<OrderItemResponse> orderItems,
                OrderAddressResponse snapshotAddress,
                LocalDateTime createdAt
) {
        @Builder
        public record OrderItemResponse(
                        UUID productId,
                        String productName,
                        Long price,
                        Long quantity,
                        String orderItemStatus) {
        }

        @Builder
        public record OrderAddressResponse(
                        String placeName,
                        String roadName,
                        String detailName) {
        }
}