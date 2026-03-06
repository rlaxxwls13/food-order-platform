package nbcamp.food_order_platform.auth.application.dto;

public record LoginAuthCommand(
        String username,
        String password
) {
}
