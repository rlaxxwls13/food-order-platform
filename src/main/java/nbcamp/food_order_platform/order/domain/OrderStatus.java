package nbcamp.food_order_platform.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    /* stateDiagram
        [*] -->  CREATED : 주문 생성
        CREATED --> PAID : 결제 완료
        CREATED --> CANCELED : 사용자 / 시스템취소 (미결제)

        PAID --> ACCEPTED : 가게 승인 (조리 시작 / 상품 준비)
        PAID -->  REJECTED : 가게 거절(재고 없음 / 가게 사정 등)

        ACCEPTED --> REFUNDED : 관리자 전용 취소
        REJECTED --> REFUNDED : 자동 환불 처리

        ACCEPTED --> COMPLETED : 배송/픽업 완료

       COMPLETED --> REFUNDED 현재 설계 단계에선 불가x

    * */
    CREATED("생성 완료"),
    PAID("결제 완료"),
    CANCELED("결제 취소"),// 결제 전 취소
    STORE_ACCEPTED("가게 승인"),
    STORE_REJECTED("가게 취소"),
    REFUNDED("환불 완료"),
    COMPLETED("배달 완료");

    private final String description;
}
