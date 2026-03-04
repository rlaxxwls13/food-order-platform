package nbcamp.food_order_platform.product.presentation.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PostProductReqDto {
    private UUID storeId;
    private String name;
    private int price;
    private int stockQuantity;
    private boolean useAi;
    private String description;
}
