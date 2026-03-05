package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderItemStatus;

import java.util.UUID;

@Builder
public record OrderItemResDto(
        //상품 주문 Dto
        UUID productId,
        String productName,
        Long price ,
        Long quantity,
        OrderItemStatus status
) {
}
