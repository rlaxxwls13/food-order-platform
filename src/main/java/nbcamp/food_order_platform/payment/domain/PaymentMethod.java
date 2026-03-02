package nbcamp.food_order_platform.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CARD("카드"); //현재 결제수단 선결제 카드만 추후 확장예정
    public final String description;
}