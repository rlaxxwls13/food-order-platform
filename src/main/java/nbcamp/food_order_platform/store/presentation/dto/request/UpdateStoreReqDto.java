package nbcamp.food_order_platform.store.presentation.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateStoreReqDto {

    private Long ownerId;
    private String name;
    private UUID regionCode;
    private String regionDetail;
    private List<UUID> categoryIds;

}
