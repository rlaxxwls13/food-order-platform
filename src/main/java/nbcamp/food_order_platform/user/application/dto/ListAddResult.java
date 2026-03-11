package nbcamp.food_order_platform.user.application.dto;

import java.util.UUID;

public record ListAddResult(
        UUID addressId,
        String placeName,
        String roadName,
        String detailName
) {
}
