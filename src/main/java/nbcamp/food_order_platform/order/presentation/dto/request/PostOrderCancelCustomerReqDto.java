package nbcamp.food_order_platform.order.presentation.dto.request;

import lombok.Builder;

@Builder
public record PostOrderCancelCustomerReqDto(
        String reason
) {
}
