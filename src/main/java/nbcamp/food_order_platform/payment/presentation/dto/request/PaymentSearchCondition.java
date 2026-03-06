package nbcamp.food_order_platform.payment.presentation.dto.request;

import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import java.time.LocalDateTime;

public record PaymentSearchCondition(
        PaymentStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate) {
}
