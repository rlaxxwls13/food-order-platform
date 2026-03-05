package nbcamp.food_order_platform.order.application.dto.query;

import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSearchQuery(
        UUID userId,           // 특정 유저의 주문만
        UUID storeId,          // 특정 가게의 주문만
        OrderStatus status,    // 특정 상태(배달중 등)만
        LocalDateTime start,   // 기간 조회 시작
        LocalDateTime end      // 기간 조회 종료
) {}