package nbcamp.food_order_platform.user.presentation.dto;

public record PatchRoleReqDto(
        String username,
        Long userId,
        String role
){
}
