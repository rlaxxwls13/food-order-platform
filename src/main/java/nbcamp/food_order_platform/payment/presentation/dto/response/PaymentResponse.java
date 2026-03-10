package nbcamp.food_order_platform.payment.presentation.dto.response;

import nbcamp.food_order_platform.payment.application.dto.result.PaymentResult;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        Long totalAmount,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt) {

    public static PaymentResponse from(PaymentResult result) {
        if (result == null) return null;
        return new PaymentResponse(
                result.paymentId(),
                result.orderId(),
                result.amount(),
                result.status(),
                result.createdAt()
        );
    }
}
