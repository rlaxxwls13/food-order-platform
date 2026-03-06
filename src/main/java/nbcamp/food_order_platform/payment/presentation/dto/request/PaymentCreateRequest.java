package nbcamp.food_order_platform.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import java.util.UUID;

public record PaymentCreateRequest(
        @NotNull UUID orderId,
        @NotNull PaymentMethod method,
        @Positive Long amount
) {}