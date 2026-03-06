package nbcamp.food_order_platform.payment.presentation.dto.response;

import java.util.UUID;

public record PaymentCancelResponse(
        UUID paymentId,
        String status
) {}