package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.application.dto.result.OrderAddressInfo;
import nbcamp.food_order_platform.order.application.dto.result.OrderItemInfo;
import nbcamp.food_order_platform.order.application.dto.result.OrderResult;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        public static OrderResponse from(OrderResult result) {
                if (result == null) return null;
                return OrderResponse.builder()
                                .orderId(result.orderId())
                                .storeId(result.storeId())
                                .storeName(result.storeName())
                                .orderStatus(result.status())
                                .totalAmount(result.totalPrice())
                                .orderItems(result.items() == null ? List.of() : result.items().stream()
                                                .map(OrderResponse::fromItem)
                                                .collect(Collectors.toList()))
                                .snapshotAddress(fromAddress(result.address()))
                                .createdAt(result.createdAt())
                                .build();
        }

        private static OrderItemResponse fromItem(OrderItemInfo item) {
                if (item == null) return null;
                return OrderItemResponse.builder()
                                .productId(item.productId())
                                .productName(item.productName())
                                .price(item.price())
                                .quantity(item.quantity())
                                .orderItemStatus(item.status() == null ? null : item.status().name())
                                .build();
        }

        private static OrderAddressResponse fromAddress(OrderAddressInfo address) {
                if (address == null) return null;
                return OrderAddressResponse.builder()
                                .placeName(address.placeName())
                                .roadName(address.roadName())
                                .detailName(address.detailName())
                                .build();
        }

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
