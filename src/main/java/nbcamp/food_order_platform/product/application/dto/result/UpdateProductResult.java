package nbcamp.food_order_platform.product.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdateProductResult {
    private UUID productId;
    private UUID storeId;
    private String name;
    private int price;
    private int stockQuantity;
    private String description;
    private boolean isHidden;
    private LocalDateTime updatedAt;

    public UpdateProductResult(UpdateProductResult result) {
        this.productId = result.productId;
        this.storeId = result.storeId;
        this.name = result.name;
        this.price = result.price;
        this.stockQuantity = result.stockQuantity;
        this.description = result.description;
        this.isHidden = result.isHidden;
        this.updatedAt = result.updatedAt;
    }
}
