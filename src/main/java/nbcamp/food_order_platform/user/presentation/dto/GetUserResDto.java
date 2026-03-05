package nbcamp.food_order_platform.user.presentation.dto;

public record GetUserResDto(
        Long userId,
        String username,
        String role
) {
}
