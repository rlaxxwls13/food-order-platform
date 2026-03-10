package nbcamp.food_order_platform.user.application.dto;

public record GetMyInfoResult(
        String nickname,
        String email,
        String role

) {
}
