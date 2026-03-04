package nbcamp.food_order_platform.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import nbcamp.food_order_platform.global.common.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "p_order_item")
@NoArgsConstructor
@Getter
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 상품 머지후 교체
    //상품 ID
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_id")
//    private Product product;
    @Column(name = "product_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID productId;

    //가게 머지후 교체
    //가게 ID
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id")
//    private Store store;
    @Column(name = "store_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID storeId;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus orderItemStatus;

    @Column(nullable = true)
    private Long canceledQuantity = 0L;

    @Column(nullable = true)
    private String reason;

    private LocalDateTime deleted_at;

    //부분 취소 처리
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

    //현재 수량에 따른 금액
    public Long calculateCurrentAmount() {
        return this.price * this.quantity;
    }
}