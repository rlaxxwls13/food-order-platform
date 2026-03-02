package nbcamp.food_order_platform.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderItemStatus {

    NORMAL("수량 남은 상품"),
    CANCELED("전체 취소된 상품"); //OWNER , MANAGER , MASTER

    private final String description;
}