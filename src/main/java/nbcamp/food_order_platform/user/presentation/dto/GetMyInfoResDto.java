package nbcamp.food_order_platform.user.presentation.dto;

import nbcamp.food_order_platform.user.application.dto.AddressInfo;

import java.util.List;

public record GetMyInfoResDto(
        String nickname,
        String email,
        String role,
        List<AddressInfo> addressInfos
) {
}
