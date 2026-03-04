package nbcamp.food_order_platform.product.presentation.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PostProductResDto {
    private UUID productId;
    private UUID storeId;
    private String name;
    private Long price;
    private Integer stockQuantity;
    private String originalDescription;
    private String generatedDescription;
    private Boolean isHidden;
    private LocalDateTime createdAt;
}
