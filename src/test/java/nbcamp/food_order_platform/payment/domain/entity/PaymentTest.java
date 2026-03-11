package nbcamp.food_order_platform.payment.domain.entity;

import nbcamp.food_order_platform.order.domain.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PaymentTest {

    @Test
    @DisplayName("결제 취소: READY 상태에서 취소 시 CANCELLED 상태가 되고 환불 금액은 0원이다")
    void cancel_readyStatus_success() {
        // given
        Order order = mock(Order.class);
        Payment payment = Payment.create(order, 10000L, PaymentMethod.CARD);

        // when
        payment.cancel();

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCanceledAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("결제 취소: COMPLETED 상태에서 취소 시 REFUNDED 상태가 되고 전액 환불된다")
    void cancel_completedStatus_success() {
        // given
        Order order = mock(Order.class);
        Payment payment = Payment.create(order, 10000L, PaymentMethod.CARD);
        payment.complete();

        // when
        payment.cancel();

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getCanceledAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("결제 취소: 이미 취소된 상태(CANCELLED)에서 재호출 시 상태가 유지된다 (멱등성)")
    void cancel_alreadyCancelled_noChange() {
        // given
        Order order = mock(Order.class);
        Payment payment = Payment.create(order, 10000L, PaymentMethod.CARD);
        payment.cancel();

        // when
        payment.cancel();

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCanceledAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("결제 취소: 이미 환불된 상태(REFUNDED)에서 재호출 시 상태가 유지된다 (멱등성)")
    void cancel_alreadyRefunded_noChange() {
        // given
        Order order = mock(Order.class);
        Payment payment = Payment.create(order, 10000L, PaymentMethod.CARD);
        payment.complete();
        payment.cancel();

        // when
        payment.cancel();

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getCanceledAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("결제 취소: FAILED 상태에서 취소 시도 시 예외가 발생한다")
    void cancel_failedStatus_throwsException() {
        // given
        Order order = mock(Order.class);
        Payment payment = Payment.create(order, 10000L, PaymentMethod.CARD);
        payment.fail();

        // when & then
        assertThatThrownBy(payment::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("취소/환불 처리는 READY 또는 COMPLETED 상태에서만 가능합니다.");
    }
}
