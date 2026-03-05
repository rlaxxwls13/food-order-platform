package nbcamp.food_order_platform.order.presentation.dto.request;

import nbcamp.food_order_platform.order.domain.entity.OrderItem;

import java.util.List;
import java.util.UUID;

public record CreateOrderReqDto (
        UUID storeId,
        String comment, //배달 요청 사항 (현재는 가게 only 추후 기사님에게 구현)
        List<OrderItem> items,
        UUID addressId
){

}
