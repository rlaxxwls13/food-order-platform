package nbcamp.food_order_platform.order.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrderCancelCommand(
        @NotNull UUID orderId,
        @NotBlank String reason,
        @NotNull UUID requesterId
) {}