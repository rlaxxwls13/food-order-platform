package nbcamp.food_order_platform.product.presentation.dto.response;

import lombok.Getter;
import nbcamp.food_order_platform.product.application.dto.CreateProductDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PostProductResDto {
    private UUID productId;
    private UUID storeId;
    private String name;
    private int price;
    private int stockQuantity;
    private String description;
    private boolean isHidden;
    private LocalDateTime createdAt;


    public PostProductResDto(CreateProductDto productDto) {
        this.productId = productDto.getProductId();
        this.storeId = productDto.getStoreId();
        this.name = productDto.getName();
        this.price = productDto.getPrice();
        this.stockQuantity = productDto.getStockQuantity();
        this.description = productDto.getDescription();
        this.isHidden = productDto.isHidden();
        this.createdAt = productDto.getCreateAt();
    }
}
