package nbcamp.food_order_platform.order.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrderItemCancelCommand(
        @NotNull UUID orderId,
        @NotBlank String reason,
        @NotEmpty List<ItemCancelDto> items
) {
    public record ItemCancelDto(
            @NotNull UUID orderItemId,
            @NotNull Long quantity
    ) {}
}