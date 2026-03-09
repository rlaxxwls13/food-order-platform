package nbcamp.food_order_platform.payment.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;

import java.util.UUID;

public record PaymentCreateRequest(
                @NotNull UUID orderId,
                @Min(0) Long amount,
                @NotNull PaymentMethod method) {
}