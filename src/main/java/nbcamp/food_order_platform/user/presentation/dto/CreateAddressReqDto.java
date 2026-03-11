package nbcamp.food_order_platform.user.presentation.dto;

public record CreateAddressReqDto(
        String placeName,
        String roadName,
        String detailName
) {
}
