package nbcamp.food_order_platform.order.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand;
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
    public static Order createOrder(User user, Store store, OrderCreateCommand command) {
        Order order = new Order();
        order.user = user;
        order.store = store;
        order.totalAmount = 0L; // 초기화 후 recalculate
        order.orderStatus = OrderStatus.CREATED;

        // Address 정보 스냅샷
        return order;
    }

    // 주소 스냅샷 설정

    public void setSnapshotAddress(OrderAddress address) {
        this.snapshotAddress = address;
    }

    // 주문 상품 추가 및 총액 계산
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.assignOrder(this);
        this.totalAmount = recalculateTotalAmount();
    }

    // 특정 주문 상품 취소 로직
    public void cancelOrderItem(UUID orderItemId, Long cancelCount) {
        OrderItem targetItem = this.orderItems.stream()
                .filter(item -> item.getOrderItemId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("상품이 주문 내역에 없습니다."));

        targetItem.partialCanceled(cancelCount);
        this.totalAmount = recalculateTotalAmount();

        boolean isAllCanceled = this.orderItems.stream()
                .allMatch(item -> item.getOrderItemStatus() == OrderItemStatus.CANCELED);
        // 모든 상품이 취소됫는지 확인하는로직 (일단 은 모든 상품이 취소된경우에는 상품취소로 간주 후에 최소 주문금액 로직 추가예정
        if (isAllCanceled) {
            this.orderStatus = OrderStatus.CANCELED;
            if (this.payment != null) {
                this.payment.cancel();
            }
        } else {
            if (this.payment != null) {
                this.payment.syncAmount(this.totalAmount);
            }
        }
    }

    private Long recalculateTotalAmount() {
        return this.orderItems.stream()
                .mapToLong(OrderItem::calculateCurrentAmount)
                .sum();
    }

    // 사용자 주문 취소 (CREATED, PAID 상태만 가능)
    public void cancelByUser() {
        if (this.orderStatus != OrderStatus.CREATED && this.orderStatus != OrderStatus.PAID) {
            throw new IllegalStateException("주문 취소는 결제 전(CREATED) 또는 결제 완료(PAID) 상태에서만 가능합니다.");
        }
        this.orderStatus = OrderStatus.CANCELED;
        this.orderItems.forEach(item -> item.partialCanceled(item.getQuantity()));
    }

    // 관리자 주문 강제 취소 (상태 무관)
    public void cancelByAdmin() {
        this.orderStatus = OrderStatus.CANCELED;
        this.orderItems.forEach(item -> item.partialCanceled(item.getQuantity()));
        if (this.payment != null) {
            this.payment.cancel();
        }
    }

    // 가게 사장 주문 승인 (PAID -> STORE_ACCEPTED)
    public void acceptByOwner() {
        if (this.orderStatus != OrderStatus.PAID) {
            throw new IllegalStateException("결제가 완료(PAID)된 주문만 승인할 수 있습니다.");
        }
        this.orderStatus = OrderStatus.STORE_ACCEPTED;
    }

    // 가게 사장 주문 거절 (PAID -> STORE_REJECTED)
    public void rejectByOwner() {
        if (this.orderStatus != OrderStatus.PAID) {
            throw new IllegalStateException("결제가 완료(PAID)된 주문만 거절할 수 있습니다.");
        }
        this.orderStatus = OrderStatus.STORE_REJECTED;
        this.orderItems.forEach(item -> item.partialCanceled(item.getQuantity()));
        if (this.payment != null) {
            this.payment.cancel();
        }
    }

    // 시스템 등 내부 상태 업데이트 (단순 변경용)
    public void updateStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}