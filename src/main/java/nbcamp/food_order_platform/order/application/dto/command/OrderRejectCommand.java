package nbcamp.food_order_platform.order.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record OrderRejectCommand(
        @NotBlank(message = "거절/취소 사유를 입력해주세요.") String reason
) {}

