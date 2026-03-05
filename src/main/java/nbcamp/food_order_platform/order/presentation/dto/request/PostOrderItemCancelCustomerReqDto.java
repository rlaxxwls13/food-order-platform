package nbcamp.food_order_platform.order.presentation.dto.request;

import java.util.List;

public record PostOrderItemCancelCustomerReqDto(
        String reason,
        List<OrderItemCancelDto> item
) {
}
