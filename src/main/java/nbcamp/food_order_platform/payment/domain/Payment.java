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

    @Column(nullable = false)
    private Long total_amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus payment_status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod payment_method;


    @Column(nullable = false)
    private boolean is_deleted = false;

    private Long canceled_amount = 0L;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime created_at;

    private LocalDateTime deleted_at;

   // 주문 금액과 결제 정보 동기화
    public void syncAmount(Long newOrderTotal) {
        long difference = this.total_amount - newOrderTotal;

        if (difference > 0) {
            this.canceled_amount += difference;
            this.total_amount = newOrderTotal;
        }
    }
}