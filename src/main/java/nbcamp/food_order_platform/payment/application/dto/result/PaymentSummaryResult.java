package nbcamp.food_order_platform.payment.application.dto.result;

import lombok.Builder;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentSummaryResult(
        UUID paymentId,
        Long amount,
        LocalDateTime createdAt,
        PaymentStatus status,
        PaymentMethod method
) {}