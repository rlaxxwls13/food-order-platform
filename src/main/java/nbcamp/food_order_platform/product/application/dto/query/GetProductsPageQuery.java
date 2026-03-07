package nbcamp.food_order_platform.product.application.dto.query;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GetProductsPageQuery {
    private UUID storeId;
    private String keyword;

    public static GetProductsPageQuery from(UUID storeId, String keyword) {
        return GetProductsPageQuery.builder()
                .storeId(storeId)
                .keyword(keyword)
                .build();
    }
}
