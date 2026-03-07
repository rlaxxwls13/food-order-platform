package nbcamp.food_order_platform.product.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdateProductCommand {
    private UUID productId;
    private String name;
    private String description;
    private Integer addStockQuantity;
    private Integer setStockQuantity;
    private Integer price;
    private Boolean useAi;
}
