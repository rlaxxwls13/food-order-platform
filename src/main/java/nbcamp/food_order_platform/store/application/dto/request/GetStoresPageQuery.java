package nbcamp.food_order_platform.store.application.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GetStoresPageQuery {
    private UUID regionCode;
    private UUID categoryId;
    private String storeName;

    public static GetStoresPageQuery from(UUID regionCode, UUID categoryId, String storeName) {
        return GetStoresPageQuery.builder()
                .regionCode(regionCode)
                .categoryId(categoryId)
                .storeName(storeName)
                .build();
    }
}
