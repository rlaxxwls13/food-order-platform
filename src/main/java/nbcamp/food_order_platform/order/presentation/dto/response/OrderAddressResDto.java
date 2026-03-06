package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;

@Builder
public record OrderAddressResDto(
        String placeName,
        String roadName,
        String detailName
) {
