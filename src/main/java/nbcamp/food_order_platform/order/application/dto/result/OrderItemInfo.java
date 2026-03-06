package nbcamp.food_order_platform.order.application.dto.result;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderItemStatus;
import java.util.UUID;

@Builder
public record OrderItemInfo(
        UUID productId,
        String productName,
        Long price,
        Long quantity,
        OrderItemStatus status
) {}