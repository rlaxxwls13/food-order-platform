package nbcamp.food_order_platform.payment.application.dto.result;

import lombok.Builder;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResult(
        UUID paymentId,
        UUID orderId,
        Long amount,
        PaymentStatus status,
        PaymentMethod method,
        LocalDateTime createdAt,
        UserInfo userInfo // 관리자용 유저 정보 조각
) {
    public record UserInfo(Long userId, String userName) {}
}