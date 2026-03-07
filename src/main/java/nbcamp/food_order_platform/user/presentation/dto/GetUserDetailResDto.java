package nbcamp.food_order_platform.user.presentation.dto;

public record GetUserDetailResDto(
        Long userId,
        String username,
        String role,
        String email
        ) {
}
