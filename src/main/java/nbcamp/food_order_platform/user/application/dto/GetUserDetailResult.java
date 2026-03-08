package nbcamp.food_order_platform.user.application.dto;

public record GetUserDetailResult(
        Long userId,
        String username,
        String role,
        String email
) {
}
