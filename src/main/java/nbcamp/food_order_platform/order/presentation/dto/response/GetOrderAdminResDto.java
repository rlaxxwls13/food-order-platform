package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record GetOrderAdminResDto(
        //관리자 주문 상세 조회 Dto
        UUID orderId,
        UUID storeId,
        OrderAddressResDto address,
        List<OrderItemResDto> Items,
        String comment

) {
}
