package nbcamp.food_order_platform.payment.application;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.payment.application.dto.command.PaymentCreateCommand;
import nbcamp.food_order_platform.payment.application.dto.query.PaymentSearchQuery;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentResult;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentSummaryResult;
import nbcamp.food_order_platform.payment.application.service.PaymentService;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import nbcamp.food_order_platform.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"prod", "local"})
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager em;

    // 결제 초기화 시 READY 결제가 저장되는지 검증
    @Test
    @DisplayName("결제 초기화 성공: READY 상태의 결제가 저장된다")
    void initiatePayment_success() {
        Long userId = 5001L;
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        seedUser(userId);
        seedStore(storeId, userId, "결제가게");
        seedOrder(orderId, userId, storeId, OrderStatus.CREATED, 10000L);

        AuthUser authUser = new AuthUser(userId, "user" + userId, "CUSTOMER");
        PaymentResult response = paymentService.initiatePayment(
                new PaymentCreateCommand(orderId, PaymentMethod.CARD, 10000L, userId),
                authUser
        );
        flushAndClear();

        Payment saved = paymentRepository.findById(response.paymentId()).orElseThrow();
        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(saved.getTotalAmount()).isEqualTo(10000L);
        assertThat(saved.getOrder().getOrderId()).isEqualTo(orderId);
    }

    // 결제 취소 시 결제/주문 상태가 취소로 변경되는지 검증
    @Test
    @DisplayName("결제 취소 성공: 결제와 주문이 취소 상태로 변경된다")
    void cancelPayment_success() {
        Long userId = 5002L;
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        seedUser(userId);
        seedStore(storeId, userId, "취소가게");
        seedOrder(orderId, userId, storeId, OrderStatus.CREATED, 12000L);

        AuthUser authUser = new AuthUser(userId, "user" + userId, "CUSTOMER");
        PaymentResult response = paymentService.initiatePayment(
                new PaymentCreateCommand(orderId, PaymentMethod.CARD, 12000L, userId),
                authUser
        );

        paymentService.cancelPayment(response.paymentId(), authUser);
        flushAndClear();

        Payment saved = paymentRepository.findById(response.paymentId()).orElseThrow();
        Order order = orderRepository.findById(orderId).orElseThrow();

        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    // 결제 검색 시 상태 필터가 정상 적용되는지 검증
    @Test
    @DisplayName("결제 검색 성공: 상태 필터가 적용된다")
    void searchPaymentsCustomer_success() {
        Long userId = 5003L;
        UUID storeId = UUID.randomUUID();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        seedUser(userId);
        seedStore(storeId, userId, "검색가게");
        seedOrder(orderId1, userId, storeId, OrderStatus.CREATED, 8000L);
        seedOrder(orderId2, userId, storeId, OrderStatus.CREATED, 9000L);

        AuthUser authUser = new AuthUser(userId, "user" + userId, "CUSTOMER");
        PaymentResult payment1 = paymentService.initiatePayment(
                new PaymentCreateCommand(orderId1, PaymentMethod.CARD, 8000L, userId),
                authUser
        );
        PaymentResult payment2 = paymentService.initiatePayment(
                new PaymentCreateCommand(orderId2, PaymentMethod.CARD, 9000L, userId),
                authUser
        );

        paymentService.completePayment(payment2.paymentId(), authUser);
        flushAndClear();

        Page<PaymentSummaryResult> result = paymentService.searchPaymentsCustomer(
                authUser,
                new PaymentSearchQuery(PaymentStatus.READY, null, null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).paymentId()).isEqualTo(payment1.paymentId());
        assertThat(result.getContent().get(0).status()).isEqualTo(PaymentStatus.READY);
    }

    private void seedUser(Long userId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("""
            insert into p_user (
                user_id, username, nickname, email, password, role,
                created_at, created_by, updated_at, updated_by
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                userId,
                "user" + userId,
                "nickname" + userId,
                "user" + userId + "@test.com",
                "password",
                "CUSTOMER",
                now,
                userId,
                now,
                userId
        );
    }

    private void seedStore(UUID storeId, Long ownerId, String name) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("""
            insert into p_store (
                store_id, owner_id, name, total_rating_sum, review_count, version,
                created_at, created_by, updated_at, updated_by
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                storeId,
                ownerId,
                name,
                0,
                0,
                0L,
                now,
                ownerId,
                now,
                ownerId
        );
    }

    private void seedOrder(UUID orderId, Long userId, UUID storeId, OrderStatus status, long totalAmount) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("""
            insert into p_order (
                order_id, user_id, store_id, total_amount, order_status,
                place_name, road_name, detail_name,
                created_at, created_by, updated_at, updated_by
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                orderId,
                userId,
                storeId,
                totalAmount,
                status.name(),
                "집",
                "도로명",
                "상세",
                now,
                userId,
                now,
                userId
        );
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}

