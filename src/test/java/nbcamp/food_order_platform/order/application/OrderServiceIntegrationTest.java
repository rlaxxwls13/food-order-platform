package nbcamp.food_order_platform.order.application;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderCreateRequest;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderSearchCondition;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderResponse;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderSummaryResponse;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager em;

    // 주문 생성 시 주문/아이템 저장과 재고 감소가 반영되는지 검증
    @Test
    @DisplayName("주문 생성 성공: 주문/주문아이템이 저장되고 재고가 감소한다")
    void createOrder_success() {
        Long userId = 4001L;
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        seedUser(userId);
        seedStore(storeId, userId, "주문가게");
        seedProduct(productId, storeId, "후라이드", 10, 3000);
        seedAddress(addressId, userId);

        OrderCreateRequest.OrderItemRequest item = new OrderCreateRequest.OrderItemRequest(productId, 2L);
        OrderCreateRequest request = new OrderCreateRequest(storeId, "요청사항", List.of(item), addressId);

        OrderResponse response = orderService.createOrder(request, userId);
        flushAndClear();

        Order saved = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(saved.getTotalAmount()).isEqualTo(6000L);
        assertThat(saved.getOrderItems()).hasSize(1);
        assertThat(saved.getSnapshotAddress()).isNotNull();

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getQuantity()).isEqualTo(8);
    }

    // 고객 주문 검색 시 상태 필터가 정상 적용되는지 검증
    @Test
    @DisplayName("고객 주문 검색 성공: 상태 필터가 적용된다")
    void searchOrdersCustomer_success() {
        Long userId = 4002L;
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        seedUser(userId);
        seedStore(storeId, userId, "검색가게");
        seedProduct(productId, storeId, "양념치킨", 10, 2500);
        seedAddress(addressId, userId);

        UUID createdOrderId = createOrder(userId, storeId, productId, addressId, 1L);
        UUID paidOrderId = createOrder(userId, storeId, productId, addressId, 1L);

        Order paidOrder = orderRepository.findById(paidOrderId).orElseThrow();
        paidOrder.updateStatus(OrderStatus.PAID);
        flushAndClear();

        Page<OrderSummaryResponse> result = orderService.searchOrdersCustomer(
                userId,
                new OrderSearchCondition(OrderStatus.CREATED, null, null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).orderId()).isEqualTo(createdOrderId);
        assertThat(result.getContent().get(0).orderStatus()).isEqualTo(OrderStatus.CREATED);
    }

    private UUID createOrder(Long userId, UUID storeId, UUID productId, UUID addressId, long quantity) {
        OrderCreateRequest.OrderItemRequest item = new OrderCreateRequest.OrderItemRequest(productId, quantity);
        OrderCreateRequest request = new OrderCreateRequest(storeId, "요청", List.of(item), addressId);
        OrderResponse response = orderService.createOrder(request, userId);
        flushAndClear();
        return response.orderId();
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

    private void seedProduct(UUID productId, UUID storeId, String name, int quantity, int price) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update("""
            insert into p_product (
                product_id, store_id, name, description, is_hidden, quantity, price, version,
                created_at, created_by, updated_at, updated_by
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                productId,
                storeId,
                name,
                "설명",
                false,
                quantity,
                price,
                0L,
                now,
                1L,
                now,
                1L
        );
    }

    private void seedAddress(UUID addressId, Long userId) {
        jdbcTemplate.update("""
            insert into p_address (
                address_id, user_id, place_name, road_name, detail_name
            ) values (?, ?, ?, ?, ?)
            """,
                addressId,
                userId,
                "집",
                "도로명",
                "상세"
        );
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
