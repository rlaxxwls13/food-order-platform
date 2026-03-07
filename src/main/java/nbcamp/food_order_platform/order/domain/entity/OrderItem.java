package nbcamp.food_order_platform.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.product.domain.entity.Product;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity(name = "p_order_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)

    @Column(name = "order_item_id", updatable = false, nullable = false)
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 상품 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus orderItemStatus;

    @Builder.Default
    @Column(nullable = true)
    private Long canceledQuantity = 0L;

    @Column(nullable = true)
    private String reason;

    // 연관관계 편의 메서드
    public void assignOrder(Order order) {
        this.order = order;
    }

    // 주문 상품 생성 팩토리 메서드
    public static OrderItem create(Product product, Long quantity) {
        return OrderItem.builder()
                .product(product)
                .productName(product.getName())
                .quantity(quantity)
                .price((long) product.getPrice())
                .orderItemStatus(OrderItemStatus.NORMAL)
                .canceledQuantity(0L)
                .build();
    }

    // 부분 취소 처리
    public void partialCanceled(Long count) {
        if (this.quantity < count) {
            throw new IllegalArgumentException("취소 요청 수량이 현재 수량보다 많습니다.");
        }

        this.quantity -= count;
        this.canceledQuantity += count;

        // 수량이 0이 되면 상태를 전체 취소로 변경
        if (this.quantity == 0) {
            this.orderItemStatus = OrderItemStatus.CANCELED;
        }
    }

    // 현재 수량에 따른 금액
    public Long calculateCurrentAmount() {
        return this.price * this.quantity;
    }
}