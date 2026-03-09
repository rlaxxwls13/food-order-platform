package nbcamp.food_order_platform.product.presentation.dto.request;

import lombok.Getter;

@Getter
public class PatchProductReqDto {
    private String name;
    private Integer price;
    private Boolean useAi;
    private String description;
    private Integer addStockQuantity;
    private Integer setStockQuantity;
}
