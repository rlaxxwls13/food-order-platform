package nbcamp.food_order_platform.payment.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    // 추후 PG 사에 연결시에 세션 만료시간과 동일하게 맞출 예정
    READY("결제 화면"),
    FAILED("결제 실패"),// 시간 초과 또는 잔액보족 등 COMPLETED 전단계 취소 발생시
    COMPLETED("결제 성공"), //결제 성공
    CANCELLED("결제 완료 후 시간 내 취소"); //결제액 전체 환불

    public final String description;
}