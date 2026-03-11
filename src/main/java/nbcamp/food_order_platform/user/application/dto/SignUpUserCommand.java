package nbcamp.food_order_platform.user.application.dto;

import nbcamp.food_order_platform.user.presentation.dto.SignupAddReqDto;
import nbcamp.food_order_platform.user.presentation.dto.SignupRequestDto;

public record SignUpUserCommand(
        SignupRequestDto userReqDto,
        SignupAddReqDto addReqDto
) {
}
