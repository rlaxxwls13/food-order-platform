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

    // 결제 시도 (READY)
    public void ready() {
        this.paymentStatus = PaymentStatus.READY;
    }

    // 결제 완료 (COMPLETED)
    public void complete() {
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    // 결제 실패 (FAILED)
    public void fail() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    // 주문 금액과 결제 정보 동기화
    public void syncAmount(Long newOrderTotal) {
        long difference = this.totalAmount - newOrderTotal;

        if (difference > 0) {
            this.canceledAmount += difference;
            this.totalAmount = newOrderTotal;
        }
    }

    // 결제 전체 취소
    public void cancel() {
        this.canceledAmount = this.totalAmount;
        this.paymentStatus = PaymentStatus.CANCELLED;
    }
}