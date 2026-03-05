package nbcamp.food_order_platform.user.presentation.dto;

import nbcamp.food_order_platform.user.domain.entity.Role;

public record GetUserResDto(
        Long userId,
        String username,
        Role role
) {
}
