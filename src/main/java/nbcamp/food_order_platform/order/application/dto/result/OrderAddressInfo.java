package nbcamp.food_order_platform.order.application.dto.result;

import lombok.Builder;

@Builder
public record OrderAddressInfo(
        String placeName,
        String roadName,
        String detailName
) {}