package nbcamp.food_order_platform.store.application.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UpdateStoreCommand {
    private UUID storeId;
    private Long ownerId;
    private String name;
    private UUID regionCode;
    private String regionDetail;
    private List<UUID> categoryIds;

    public static UpdateStoreCommand from(
            UUID storeId, Long ownerId, String name, UUID regionCode, String regionDetail, List<UUID> categoryIds
    ) {
        return UpdateStoreCommand.builder()
                .storeId(storeId)
                .ownerId(ownerId)
                .name(name)
                .regionCode(regionCode)
                .regionDetail(regionDetail)
                .categoryIds(categoryIds)
                .build();
    }
}
