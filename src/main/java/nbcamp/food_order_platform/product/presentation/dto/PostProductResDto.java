package nbcamp.food_order_platform.product.presentation.dto;

import lombok.Getter;
import nbcamp.food_order_platform.product.domain.entity.Product;

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


    public PostProductResDto(Product product) {
        this.productId = product.getId();
        this.storeId = product.getStoreId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stockQuantity = product.getQuantity();
        this.description = product.getDescription();
        this.isHidden = product.isHidden();
        this.createdAt = product.getCreatedAt();
    }
}
