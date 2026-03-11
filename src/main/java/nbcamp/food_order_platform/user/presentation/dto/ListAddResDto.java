package nbcamp.food_order_platform.user.presentation.dto;

import java.util.UUID;

public record ListAddResDto(
        UUID addressId,
        String placeName,
        String roadName,
        String detailName
) {
}
