package nbcamp.food_order_platform.product.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateProductCommand {
    private UUID storeId;
    private String name;
    private int stockQuantity;
    private int price;
    private String description;
    private boolean useAi;
}
