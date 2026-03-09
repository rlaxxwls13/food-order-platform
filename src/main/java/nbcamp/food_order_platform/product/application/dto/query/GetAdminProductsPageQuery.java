package nbcamp.food_order_platform.product.application.dto.query;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GetAdminProductsPageQuery {
    private UUID storeId;
    private String keyword;
    private Boolean includeHidden;
    private Boolean includeDeleted;

    public static GetAdminProductsPageQuery from(
            UUID storeId,
            String keyword,
            Boolean includeHidden,
            Boolean includeDeleted
    ) {
        return GetAdminProductsPageQuery.builder()
                .storeId(storeId)
                .keyword(keyword)
                .includeHidden(includeHidden)
                .includeDeleted(includeDeleted)
                .build();
    }
}
