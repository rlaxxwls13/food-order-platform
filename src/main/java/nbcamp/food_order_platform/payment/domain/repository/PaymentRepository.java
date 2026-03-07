package nbcamp.food_order_platform.payment.domain.repository;

import nbcamp.food_order_platform.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // 특정 유저의 결제 목록 조회 (Payment → Order → User 경로)
    List<Payment> findAllByOrderUserUserId(Long userId);
}
