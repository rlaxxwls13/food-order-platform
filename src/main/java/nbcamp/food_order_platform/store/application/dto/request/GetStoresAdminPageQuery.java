package nbcamp.food_order_platform.store.application.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GetStoresAdminPageQuery {
    private UUID regionCode;
    private UUID categoryId;
    private String storeName;
    private Boolean includeDeleted;

    public static GetStoresAdminPageQuery from(
            UUID regionCode,
            UUID categoryId,
            String storeName,
            Boolean includeDeleted
    ) {
        return GetStoresAdminPageQuery.builder()
                .regionCode(regionCode)
                .categoryId(categoryId)
                .storeName(storeName)
                .includeDeleted(includeDeleted)
                .build();
    }
}
