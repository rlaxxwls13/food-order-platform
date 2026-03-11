package nbcamp.food_order_platform.product.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbcamp.food_order_platform.product.application.dto.result.UpdateProductResult;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PatchProductResDto {
    private UUID productId;
    private UUID storeId;
    private String name;
    private int price;
    private int stockQuantity;
    private String description;
    private boolean isHidden;
    private LocalDateTime updatedAt;

    public PatchProductResDto(UpdateProductResult result) {
        this.productId = result.getProductId();
        this.storeId = result.getStoreId();
        this.name = result.getName();
        this.price = result.getPrice();
        this.stockQuantity = result.getStockQuantity();
        this.description = result.getDescription();
        this.isHidden = result.isHidden();
        this.updatedAt = result.getUpdatedAt();
    }
}
