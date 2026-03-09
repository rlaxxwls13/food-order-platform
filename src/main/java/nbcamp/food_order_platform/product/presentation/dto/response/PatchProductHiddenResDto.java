package nbcamp.food_order_platform.product.presentation.dto.response;

import lombok.Getter;
import nbcamp.food_order_platform.product.application.dto.result.UpdateProductHiddenResult;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PatchProductHiddenResDto {
    private UUID productId;
    private boolean isHidden;
    private LocalDateTime updatedAt;

    public PatchProductHiddenResDto(UpdateProductHiddenResult result) {
        this.productId = result.getProductId();
        this.isHidden = result.isHidden();
        this.updatedAt = result.getUpdatedAt();
    }
}
