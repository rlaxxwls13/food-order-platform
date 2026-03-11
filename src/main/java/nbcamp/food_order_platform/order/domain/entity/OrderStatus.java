package nbcamp.food_order_platform.order.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    /* stateDiagram
        [*] -->  CREATED : 주문 생성
        CREATED --> PAID : 결제 완료
        CREATED --> CANCELED : 사용자 / 시스템취소 (미결제)

        PAID --> STORE_ACCEPTED : 가게 승인 (조리 시작 / 상품 준비)
        PAID --> STORE_REJECTED : 가게 거절(재고 없음 / 가게 사정 등)

        %% 환불 완료(Refunded)는 주문 상태가 아니라 결제(Payment) 상태에서 관리
        %% PaymentStatus.REFUNDED 참고

        STORE_ACCEPTED --> COMPLETED : 배송/픽업 완료 (현재 코드 미구현)

    * */
    CREATED("생성 완료"),
    PAID("결제 완료"),
    CANCELED("결제 취소"),// 결제 전 취소
    STORE_ACCEPTED("가게 승인"),
    STORE_REJECTED("가게 취소"),
    COMPLETED("배달 완료");

    private final String description;
}
