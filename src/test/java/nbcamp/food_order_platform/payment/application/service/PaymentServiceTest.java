package nbcamp.food_order_platform.payment.application.service;

import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import nbcamp.food_order_platform.payment.domain.repository.PaymentRepository;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentCreateRequest;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentResponse;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 초기화 성공: 주문 상태가 CREATED일 때 결제를 READY 상태로 생성한다")
    void initiatePayment_success() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();
        PaymentCreateRequest request = new PaymentCreateRequest(orderId, 10000L, PaymentMethod.CARD);

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);

        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);
        when(order.getOrderStatus()).thenReturn(OrderStatus.CREATED);
        when(order.getOrderId()).thenReturn(orderId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Payment payment = mock(Payment.class);
        when(payment.getPaymentId()).thenReturn(UUID.randomUUID());
        when(payment.getOrder()).thenReturn(order);
        when(payment.getTotalAmount()).thenReturn(10000L);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.READY);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // when
        PaymentResponse response = paymentService.initiatePayment(request, userId);

        // then
        verify(paymentRepository).save(any(Payment.class));
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.READY);
    }

    @Test
    @DisplayName("결제 초기화 실패: 주문 상태가 CREATED가 아니면 VALIDATION_FAILED 예외가 발생한다")
    void initiatePayment_fail_invalid_order_status() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();
        PaymentCreateRequest request = new PaymentCreateRequest(orderId, 10000L, PaymentMethod.CARD);

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);

        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PAID); // Not CREATED

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.initiatePayment(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("결제 가능한 주문 상태가 아닙니다.");
    }

    @Test
    @DisplayName("결제 완료 성공: 결제 완료 처리 시 주문 상태도 PAID로 변경된다")
    void completePayment_success() {
        // given
        UUID paymentId = UUID.randomUUID();
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);

        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);
        when(order.getOrderId()).thenReturn(UUID.randomUUID());

        Payment payment = mock(Payment.class);
        when(payment.getOrder()).thenReturn(order);
        when(payment.getPaymentId()).thenReturn(paymentId);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // when
        paymentService.completePayment(paymentId, userId);

        // then
        verify(payment).complete();
        verify(order).updateStatus(OrderStatus.PAID);
    }

    @Test
    @DisplayName("결제 취소 성공: 결제 취소 시 연관된 주문도 취소된다")
    void cancelPayment_success() {
        // given
        UUID paymentId = UUID.randomUUID();
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getUserId()).thenReturn(userId);

        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);

        Payment payment = mock(Payment.class);
        when(payment.getOrder()).thenReturn(order);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // when
        paymentService.cancelPayment(paymentId, userId);

        // then
        verify(payment).cancel();
        verify(order).cancelByUser();
    }
}
