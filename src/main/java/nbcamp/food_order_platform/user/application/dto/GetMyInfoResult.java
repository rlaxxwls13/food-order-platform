package nbcamp.food_order_platform.user.application.dto;

import java.util.List;

public record GetMyInfoResult(
        String nickname,
        String email,
        String role,
        List<AddressInfo> addressInfos) {
}
