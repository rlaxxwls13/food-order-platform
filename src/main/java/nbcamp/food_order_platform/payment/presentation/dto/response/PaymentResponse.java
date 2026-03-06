package nbcamp.food_order_platform.payment.presentation.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        Long amount,
        String status,
        LocalDateTime createdAt
) {}