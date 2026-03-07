package nbcamp.food_order_platform.payment.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.order.domain.entity.Order;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_payment")
@Getter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID paymentId;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "canceled_amount")
    private Long canceledAmount = 0L;

    // 결제 생성 팩토리 메서드 (Step 1: READY 상태로 생성)
    public static Payment create(Order order, Long amount, PaymentMethod method) {
        Payment payment = new Payment();
        payment.order = order;
        payment.totalAmount = amount;
        payment.paymentMethod = method;
        payment.paymentStatus = PaymentStatus.READY;
        payment.canceledAmount = 0L;
        return payment;
    }

    // 주문 금액과 결제 정보 동기화
    public void syncAmount(Long newOrderTotal) {
        long difference = this.totalAmount - newOrderTotal;
        if (difference > 0) {
            this.canceledAmount += difference;
            this.totalAmount = newOrderTotal;
        }
    }

    // 결제 완료 처리
    public void complete() {
        if (this.paymentStatus != PaymentStatus.READY) {
            throw new IllegalStateException("결제 완료 처리는 READY 상태에서만 가능합니다.");
        }
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    // 결제 실패 처리 (기타 사유)
    public void fail() {
        if (this.paymentStatus != PaymentStatus.READY) {
            throw new IllegalStateException("결제 실패 처리는 READY 상태에서만 가능합니다.");
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }

    // 결제 강제 취소 (전액 환불)
    public void cancel() {
        this.canceledAmount = this.totalAmount;
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    // 15분 경과 여부 확인 후 자동 실패 처리 로직
    public void failIfTimeout() {
        if (this.paymentStatus == PaymentStatus.READY && this.getCreatedAt() != null) {
            if (this.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
                this.paymentStatus = PaymentStatus.FAILED;
            }
        }
    }
}