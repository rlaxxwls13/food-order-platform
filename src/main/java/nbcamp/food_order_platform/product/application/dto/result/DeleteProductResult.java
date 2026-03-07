package nbcamp.food_order_platform.product.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class DeleteProductResult {
    private UUID productId;
    private LocalDateTime deletedAt;
    private Long deletedBy;
}
