package nbcamp.food_order_platform.order.application.dto.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrderCreateCommand(
        @NotNull UUID storeId,
        String comment,
        @NotEmpty List<OrderItemCommand> items,
        @NotNull UUID addressId,
        @NotNull Long userId
) {
    public record OrderItemCommand(
            @NotNull UUID productId,
            @Min(1) Long quantity
    ) {}
}