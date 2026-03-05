package nbcamp.food_order_platform.payment.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import java.util.UUID;

public record PaymentCreateCommand(
        @NotNull UUID orderId,
        @NotNull PaymentMethod method,
        @Positive Long amount, // 0원 이하는 결제 불가
        @NotNull UUID userId
) {}