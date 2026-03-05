package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID orderId,
        String status,
        Long totalAmount,
        List<OrderItemResDto> items //
) {}