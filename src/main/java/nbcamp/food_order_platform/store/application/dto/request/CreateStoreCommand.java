package nbcamp.food_order_platform.store.application.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CreateStoreCommand {
    private String name;
    private UUID regionCode;
    private String addressDetail;
    private List<UUID> categoryIds;

    public static CreateStoreCommand from(String name, UUID regionCode, String addressDetail, List<UUID> categoryIds) {
        return CreateStoreCommand.builder()
                .name(name)
                .regionCode(regionCode)
                .addressDetail(addressDetail)
                .categoryIds(categoryIds)
                .build();
    }
}
