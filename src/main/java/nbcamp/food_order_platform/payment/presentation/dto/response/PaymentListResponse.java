package nbcamp.food_order_platform.payment.presentation.dto.response;

import nbcamp.food_order_platform.payment.application.dto.result.PaymentSummaryResult;
import java.util.List;

public record PaymentListResponse(
        List<PaymentSummaryResult> payments
) {}