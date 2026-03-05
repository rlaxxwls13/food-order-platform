package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record PostOrderCustomerResDto(
        //사용자 주문 생성 응답용 dto
        UUID orderId,
        UUID storeId,
        OrderStatus status,
        Long totalPrice,
        List<OrderItemResDto> items,
        OrderAddressResDto address,
        LocalDateTime createdAt
) {
}
