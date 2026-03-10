package nbcamp.food_order_platform.order.application.service;

import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand;
import nbcamp.food_order_platform.order.application.dto.result.OrderResult;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Address;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.AddressRepository;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공: 정상적인 요청이 들어오면 주문이 생성되고 재고가 감소한다")
    void createOrder_success() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        User user = mock(User.class);
        Store store = mock(Store.class);
        Address address = mock(Address.class);
        Product product = mock(Product.class);
        OrderCreateCommand.OrderItemCommand itemReq = new OrderCreateCommand.OrderItemCommand(productId, 2L);
        OrderCreateCommand command = new OrderCreateCommand(storeId, "배달 요청사항", List.of(itemReq), addressId, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        
        when(address.getPlaceName()).thenReturn("집");
        when(address.getRoadName()).thenReturn("도로명주소");
        when(address.getDetailName()).thenReturn("상세주소");
        
        when(store.getId()).thenReturn(storeId);
        when(store.getName()).thenReturn("치킨집");
        when(product.getId()).thenReturn(productId);
        when(product.getStoreId()).thenReturn(storeId);

        Order order = mock(Order.class);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(order.getOrderId()).thenReturn(UUID.randomUUID());
        when(order.getStore()).thenReturn(store);
        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("user" + userId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.CREATED);
        when(order.getTotalAmount()).thenReturn(0L);
        when(order.getOrderItems()).thenReturn(List.of()); // Simplified for response mapping
        when(order.getSnapshotAddress()).thenReturn(null);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.getUserId()).thenReturn(userId);
        when(authUser.getRole()).thenReturn("CUSTOMER");

        // when
        OrderResult response = orderService.createOrder(command, authUser);

        // then
        verify(product).decreaseStock(2);
        verify(orderRepository).save(any(Order.class));
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("주문 상세 조회 성공: 주문자가 본인의 주문을 조회할 때 상세 정보를 반환한다")
    void getOrderCustomer_success() {
        // given
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(UUID.randomUUID());
        when(store.getName()).thenReturn("가게이름");

        Order order = mock(Order.class);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getUser()).thenReturn(user);
        when(order.getStore()).thenReturn(store);
        when(order.getOrderItems()).thenReturn(List.of());
        when(order.getOrderStatus()).thenReturn(OrderStatus.CREATED);
        when(order.getTotalAmount()).thenReturn(0L);
        when(order.getSnapshotAddress()).thenReturn(null);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.getUserId()).thenReturn(userId);
        when(authUser.getRole()).thenReturn("CUSTOMER");

        // when
        OrderResult response = orderService.getOrderCustomer(orderId, authUser);

        // then
        assertThat(response.orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("주문 상세 조회 실패: 다른 사용자의 주문을 조회하려고 하면 NO_PERMISSION 예외가 발생한다")
    void getOrderCustomer_fail_no_permission() {
        // given
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;
        Long otherUserId = 2L;

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(otherUserId);

        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        AuthUser authUser = mock(AuthUser.class);
        when(authUser.getUserId()).thenReturn(userId);
        when(authUser.getRole()).thenReturn("CUSTOMER");

        assertThatThrownBy(() -> orderService.getOrderCustomer(orderId, authUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NO_PERMISSION.getMessage());
    }

    @Test
    @DisplayName("주문 취소 성공: 주문자가 본인의 주문을 취소하면 주문 상태가 변경된다")
    void cancelOrderByUser_success() {
        // given
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);

        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.getUserId()).thenReturn(userId);
        when(authUser.getRole()).thenReturn("CUSTOMER");

        // when
        orderService.cancelOrderByUser(orderId, authUser);

        // then
        verify(order).cancelByUser();
    }
}
