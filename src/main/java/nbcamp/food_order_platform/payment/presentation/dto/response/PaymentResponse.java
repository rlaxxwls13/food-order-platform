package nbcamp.food_order_platform.payment.presentation.dto.response;

import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        Long totalAmount,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt) {
}