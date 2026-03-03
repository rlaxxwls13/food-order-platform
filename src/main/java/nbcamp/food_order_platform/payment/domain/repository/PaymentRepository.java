package nbcamp.food_order_platform.payment.domain.repository;

import nbcamp.food_order_platform.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
