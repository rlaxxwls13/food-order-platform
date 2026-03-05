package nbcamp.food_order_platform.order.presentation.dto.response;

import java.util.List;

public record GetOrdersAdminResDto(
        //관리자 주문 내역 조회Dto
        List<OrderSummaryAdminDto> admin
) {
}
