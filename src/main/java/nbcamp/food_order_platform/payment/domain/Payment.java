package nbcamp.food_order_platform.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.order.domain.Order;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_payment")
@Getter
@SQLDelete(sql = "UPDATE p_payment SET deleted_at = NOW()  WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

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


    @Column(nullable = false)
    private boolean is_deleted = false;

    @Column(name = "canceled_amount")
    private Long canceledAmount = 0L;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime created_at;

    private LocalDateTime deleted_at;

   // 주문 금액과 결제 정보 동기화
    public void syncAmount(Long newOrderTotal) {
        long difference = this.totalAmount - newOrderTotal;

        if (difference > 0) {
            this.canceledAmount += difference;
            this.totalAmount = newOrderTotal;
        }
    }
}