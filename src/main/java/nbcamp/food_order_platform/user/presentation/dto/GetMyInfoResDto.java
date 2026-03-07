package nbcamp.food_order_platform.user.presentation.dto;

public record GetMyInfoResDto(
        Long userId,
        String username,
        String role
) {
}
