package nbcamp.food_order_platform.payment.domain.repository;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
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
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("사용자 결제 검색: 특정 상태의 사용자 결제 내역이 정상적으로 페이징 조회되는지 확인한다")
    void searchCustomerPayments_success() {
        // given
        User user = User.builder()
                .username("testuser_p")
                .nickname("테스터_P")
                .email("test_p@example.com")
                .password("password")
                .role(Role.CUSTOMER)
                .build();
        em.persist(user);

        RegionCode rc = RegionCode.create("서울", true);
        em.persist(rc);

        Store store = new Store(user.getUserId(), "치킨집_P", rc, "상세주소", List.of());
        em.persist(store);

        Order order1 = Order.create(user, store);
        Order order2 = Order.create(user, store);
        em.persist(order1);
        em.persist(order2);

        Payment payment1 = Payment.create(order1, 10000L, PaymentMethod.CARD);
        Payment payment2 = Payment.create(order2, 20000L, PaymentMethod.CARD);
        payment2.complete();
        
        em.persist(payment1);
        em.persist(payment2);
        em.flush();
        em.clear();

        // when
        Page<Payment> result = paymentRepository.searchCustomerPayments(
                user.getUserId(), PaymentStatus.READY, null, null, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPaymentStatus()).isEqualTo(PaymentStatus.READY);
    }
}
