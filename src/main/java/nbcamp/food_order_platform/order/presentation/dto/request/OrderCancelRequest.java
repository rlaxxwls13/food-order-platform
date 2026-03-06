package nbcamp.food_order_platform.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderCancelRequest(
        @NotBlank(message = "취소 사유를 입력해주세요.") String reason
) {}