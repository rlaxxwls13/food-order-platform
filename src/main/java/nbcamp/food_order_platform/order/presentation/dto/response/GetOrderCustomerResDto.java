package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record GetOrderCustomerResDto(
        //사용자 주문 상세 응답 Dto
        UUID orderId,
        String userName,
        String storeName,
        OrderStatus status,
        Long totalPrice,
        List<OrderItemResDto> items,
        OrderAddressResDto address,
        LocalDateTime createdAt
) {
}
