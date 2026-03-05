package nbcamp.food_order_platform.order.presentation.dto.request;

import java.util.UUID;

public record OrderItemCancelDto(
        Long quantity,
        UUID orderItemId
) {
}
