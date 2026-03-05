package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import java.util.UUID;

@Builder
public record OrderCancelResponse(
        UUID orderId
) {}