package nbcamp.food_order_platform.order.domain.repository;

import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // 사용자가 선택적을 입력할 수 있는 필터 조건을 모두 고려하는 쿼리
    @Query("SELECT o FROM p_order o WHERE o.user.userId = :userId " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<Order> searchCustomerOrders(@Param("userId") Long userId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT o FROM p_order o WHERE o.store.id = :storeId " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<Order> searchOwnerOrders(@Param("storeId") UUID storeId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT o FROM p_order o WHERE (:status IS NULL OR o.orderStatus = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<Order> searchAdminOrders(@Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
