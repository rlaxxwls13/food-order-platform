package nbcamp.food_order_platform.product.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class CreateProductDto {
    private UUID storeId;
    private String name;
    private int stockQuantity;
    private int price;
    private String description;
    private boolean useAi;
}
