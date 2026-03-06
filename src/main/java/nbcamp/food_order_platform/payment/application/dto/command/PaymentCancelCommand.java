package nbcamp.food_order_platform.payment.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PaymentCancelCommand(
        @NotNull UUID paymentId,
        @NotBlank String reason,
        @NotNull UUID requesterId
) {}