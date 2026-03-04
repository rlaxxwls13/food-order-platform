package nbcamp.food_order_platform.order.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.user.domain.entity.Address;
import nbcamp.food_order_platform.user.domain.entity.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "p_order")
@Getter
@NoArgsConstructor
public class Order extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID orderId;

    //유저 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 가게 머지후 교체
    //가게 ID
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id")
//    private Store store;
    @Column(name = "store_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID store;

//    주소 머지후 교체 (교체 완료 )
//    주소 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
//    @Column(name = "address_id")
//    @JdbcTypeCode(SqlTypes.UUID)
//    private UUID addressId;
//
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Column(nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updated_at;

    private LocalDateTime deleted_at;

    //특정 주문 상품 취소
    public void cancelOrderItem(UUID orderItemId, Long cancelCount) {
        OrderItem targetItem = this.orderItems.stream()
                .filter(item -> item.getOrderItemId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 주문 내역에 없습니다."));

        targetItem.partialCanceled(cancelCount);

        this.totalAmount = recalculateTotalAmount();

        // 결제 정보 동기화 (payment 엔티티가 존재할 경우)
        if (this.payment != null) {
            this.payment.syncAmount(this.totalAmount);
        }
    }

    // 남은 상품들 기준 총액 계산
    private Long recalculateTotalAmount() {
        return this.orderItems.stream()
                .mapToLong(OrderItem::calculateCurrentAmount)
                .sum();
    }
}