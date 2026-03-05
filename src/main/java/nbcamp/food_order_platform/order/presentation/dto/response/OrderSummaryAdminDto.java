package nbcamp.food_order_platform.order.presentation.dto.response;

import lombok.Builder;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderSummaryAdminDto(
        //관리자용 주문 내역 List dto
        UUID orderId,
        String storeName,              // 가게 이름
        String representativeItemName, // 대표 상품명 (ex: 후라이드 치킨 외 1건)
        Long totalAmount,               // 총 결제 금액
        OrderStatus status,            // 상태 코드 (PAID, COMPLETED 등)
        String statusDescription,      // 상태 설명 (결제 완료, 배달 완료)
        LocalDateTime createdAt        // 주문 일시
) {
}

