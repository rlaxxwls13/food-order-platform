package nbcamp.food_order_platform.user.application.dto;

public record PatchRoleCommand(
        Long userId,
        String role
) {
}
