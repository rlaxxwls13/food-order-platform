package nbcamp.food_order_platform.order.presentation.dto.response;

import nbcamp.food_order_platform.order.application.dto.result.OrderSummaryResult;
import java.util.List;

public record OrderListResponse(
        List<OrderSummaryResult> orders
) {}