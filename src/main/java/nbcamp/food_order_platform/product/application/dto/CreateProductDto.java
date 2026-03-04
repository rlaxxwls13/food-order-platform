package nbcamp.food_order_platform.product.application.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateProductDto {
    private UUID storeId;
    private String name;
    private Integer stockQuantity;
    private Integer price;
    private boolean isHidden;
    private String originalDescription;
    private String generatedDescription;
}
