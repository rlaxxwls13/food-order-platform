package nbcamp.food_order_platform.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "p_order_item")
@NoArgsConstructor
@Getter
@SQLDelete(sql = "UPDATE p_order_item SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OrderItem {

    //UUID
    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    //주문 ID
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

    //가게 이름
    @Column(nullable = false)
    private String store_name;

    //상품 이름
    @Column(nullable = false)
    private String product_name;

    //상품 개수
    @Column(nullable = false)
    private Long quantity;

    //상품 가격
    @Column(nullable = false)
    private Long price;

    //주문 상태
    @Column(nullable = false)
    private OrderItemStatus orderItemStatus;

    //취소 수량
    @Column(nullable = true)
    private Long canceledQuantity = 0L;

    @Column(nullable = true)
    private String reason;

    private LocalDateTime deleted_at;

}
