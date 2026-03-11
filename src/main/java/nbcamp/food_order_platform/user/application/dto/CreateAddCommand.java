package nbcamp.food_order_platform.user.application.dto;

public record CreateAddCommand(
        String placeName,
        String roadName,
        String detailName
) {
}
