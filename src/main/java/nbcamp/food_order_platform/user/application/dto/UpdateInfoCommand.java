package nbcamp.food_order_platform.user.application.dto;

import nbcamp.food_order_platform.user.presentation.dto.PatchUserReqDto;

public record UpdateInfoCommand(
        Long userId,
        PatchUserReqDto dto
) {
}
