package nbcamp.food_order_platform.order.domain.repository;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles({"prod", "local"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("사용자 주문 검색: 특정 상태의 사용자 주문이 정상적으로 페이징 조회되는지 확인한다")
    void searchCustomerOrders_success() {
        // given
        User user = User.builder()
                .username("testuser")
                .nickname("테스터")
                .email("test@example.com")
                .password("password")
                .role(Role.CUSTOMER)
                .build();
        em.persist(user);

        RegionCode rc = RegionCode.create("서울", true);
        em.persist(rc);

        Store store = new Store(user.getUserId(), "치킨집", rc, "상세주소", List.of());
        em.persist(store);

        Order order1 = Order.create(user, store);
        Order order2 = Order.create(user, store);
        order2.updateStatus(OrderStatus.PAID);
        
        em.persist(order1);
        em.persist(order2);
        em.flush();
        em.clear();

        // when
        Page<Order> result = orderRepository.searchCustomerOrders(
                user.getUserId(), OrderStatus.CREATED, null, null, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getOrderStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
