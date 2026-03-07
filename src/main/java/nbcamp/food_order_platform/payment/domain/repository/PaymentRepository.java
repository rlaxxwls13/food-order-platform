package nbcamp.food_order_platform.payment.domain.repository;

import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("SELECT p FROM p_payment p WHERE p.order.user.userId = :userId " +
            "AND (:status IS NULL OR p.paymentStatus = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR p.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR p.createdAt <= :endDate)")
    Page<Payment> searchCustomerPayments(@Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT p FROM p_payment p WHERE p.order.store.id = :storeId " +
            "AND (:status IS NULL OR p.paymentStatus = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR p.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR p.createdAt <= :endDate)")
    Page<Payment> searchOwnerPayments(@Param("storeId") UUID storeId,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT p FROM p_payment p WHERE (:status IS NULL OR p.paymentStatus = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR p.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR p.createdAt <= :endDate)")
    Page<Payment> searchAdminPayments(@Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
