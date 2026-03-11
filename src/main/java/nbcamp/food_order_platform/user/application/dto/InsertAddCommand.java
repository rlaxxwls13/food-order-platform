package nbcamp.food_order_platform.user.application.dto;

public record InsertAddCommand(
        String placeName,
        String roadName,
        String detailName
) {
}
