package nbcamp.food_order_platform.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.payment.domain.Payment;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
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
@SQLDelete(sql = "UPDATE p_orders SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Order {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    //유저 머지후 교체
    //유저 ID
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
    @Column(name = "user_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID user_id;

    // 가게 머지후 교체
    //가게 ID
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "strore_id")
//    private Store store;
    @Column(name = "store_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID store;
//    @JoinColumn(name = "store_id")
//    private Store store;

    //주소 머지후 교체
    //주소 ID
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "address_id")
//    private Address address;
    @Column(name = "address_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID address_id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Column(nullable = false)
    private Long total_amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;


    // 추후 BaseEntity
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

    //추후 BaseEntity
    @LastModifiedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime updated_at;

    // soft delete 시간 기록용 필드
    private LocalDateTime deleted_at;

}
