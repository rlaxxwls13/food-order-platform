package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;

import java.util.List;

public record GetOrdersCustomerResDto(
        //사용자 주문 내역 조회Dto
        List<OrderSummaryCustomerDto> item
){
}
