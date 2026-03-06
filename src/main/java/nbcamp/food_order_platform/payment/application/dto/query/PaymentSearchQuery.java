package nbcamp.food_order_platform.payment.application.dto.query;

import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import java.time.LocalDateTime;

public record PaymentSearchQuery(
        PaymentStatus status,
        LocalDateTime from,
        LocalDateTime to
) {}