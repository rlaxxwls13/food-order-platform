package nbcamp.food_order_platform.payment.presentation.dto.response;

import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentSummaryResponse(
                UUID paymentId,
                Long totalAmount,
                LocalDateTime createdAt,
                PaymentStatus paymentStatus,
                PaymentMethod paymentMethod) {
}
