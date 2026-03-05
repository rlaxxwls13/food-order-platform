package nbcamp.food_order_platform.product.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CreateProductDto {
    private UUID storeId;
    private String name;
    private int stockQuantity;
    private int price;
    private String description;
    private boolean useAi;
    private boolean isHidden;
    private LocalDateTime createAt;

    private UUID productId;

    // 생성 요청용 생성자
    public CreateProductDto(UUID storeId, String name, int stockQuantity, int price, String description, boolean useAi) {
        this.storeId = storeId;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.price = price;
        this.description = description;
        this.useAi = useAi;
    }

    // 결과 포함 생성자
    public CreateProductDto(UUID storeId, String name, int stockQuantity, int price, String description, UUID productId, boolean isHidden, LocalDateTime createdAt) {
        this.storeId = storeId;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.price = price;
        this.description = description;
        this.productId = productId;
        this.isHidden = isHidden;
        this.createAt = createdAt;
    }
}
