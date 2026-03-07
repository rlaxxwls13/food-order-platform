package nbcamp.food_order_platform.order.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.user.domain.entity.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "p_order")
@Getter
@NoArgsConstructor
public class Order extends BaseEntity {
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID orderId;

    // 유저 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 가게 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // 주소 ID
    @Embedded
    private OrderAddress snapshotAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Column(nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    // 주문 생성 팩토리 메서드
    public static Order createOrder(User user, Store store,
            nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand command) {
        Order order = new Order();
        order.user = user;
        order.store = store;
        order.totalAmount = 0L; // 초기화 후 recalculate
        order.orderStatus = OrderStatus.CREATED;

        // Address 정보 스냅샷
        // 참고: command.addressId()를 통해 실제 주소를 조회하여 snapshotAddress를 채워야 함 (Service 레이어
        // 책임)
        // 여기선 간단히 DTO에서 변환된 정보를 받는다고 가정하거나, Service에서 직접 set 하도록 유도
        return order;
    }

    // 주소 스냅샷 설정
    public void setSnapshotAddress(OrderAddress address) {
        this.snapshotAddress = address;
    }

    // 주문 상품 추가 및 총액 계산
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        this.totalAmount = recalculateTotalAmount();
    }

    // 특정 주문 상품 취소
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

    // 주문 전체 취소 (미결제 상태)
    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
        // 모든 상품도 취소 처리
        this.orderItems.forEach(item -> item.partialCanceled(item.getQuantity()));
    }

    // 주문 환불 (결제 완료 후)
    public void refund() {
        this.orderStatus = OrderStatus.REFUNDED;
        // 모든 상품도 취소 처리
        this.orderItems.forEach(item -> item.partialCanceled(item.getQuantity()));
        // 결제도 취소
        if (this.payment != null) {
            this.payment.cancel();
        }
    }

    // 주문 상태 변경 (결제 완료 등 상태 전이 시 사용)
    public void updateStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}