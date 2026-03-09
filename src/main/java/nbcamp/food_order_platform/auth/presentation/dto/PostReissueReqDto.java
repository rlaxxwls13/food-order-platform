package nbcamp.food_order_platform.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record PostReissueReqDto(
        @NotBlank
        String refreshToken
) {
}
