package nbcamp.food_order_platform.product.presentation.dto.response;

import lombok.Getter;
import nbcamp.food_order_platform.product.application.dto.result.DeleteProductResult;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeleteProductResDto {
    private UUID productId;
    private LocalDateTime deletedAt;
    private Long deletedBy;

    public DeleteProductResDto(DeleteProductResult result) {
        this.productId = result.getProductId();
        this.deletedAt = result.getDeletedAt();
        this.deletedBy = result.getDeletedBy();
    }
}
