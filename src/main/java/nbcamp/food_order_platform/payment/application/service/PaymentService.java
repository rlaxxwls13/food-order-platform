package nbcamp.food_order_platform.payment.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentCreateRequest;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentSearchCondition;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentResponse;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentSummaryResponse;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import nbcamp.food_order_platform.payment.domain.repository.PaymentRepository;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // 결제 초기화 (READY 상태 생성)
    @Transactional
    public PaymentResponse initiatePayment(PaymentCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ORDER));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "결제 가능한 주문 상태가 아닙니다.");
        }

        Payment payment = Payment.create(order, request.amount(), request.method());
        return toPaymentResponse(paymentRepository.save(payment));
    }

    // 결제 성공 (웹훅 등에서 호출 시)
    @Transactional
    public PaymentResponse completePayment(UUID paymentId, Long userId) {
        Payment payment = findPaymentById(paymentId);
        validatePaymentOwner(payment, userId);
        payment.failIfTimeout();

        if (payment.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "결제가 15분을 초과하여 실패 처리되었습니다.");
        }

        payment.complete();
        payment.getOrder().updateStatus(OrderStatus.PAID);
        return toPaymentResponse(payment);
    }

    // 결제 단건 조회 (고객용 - 지연시간 타임아웃 검증 포함)
    @Transactional
    public PaymentResponse getPaymentCustomer(UUID paymentId, Long userId) {
        Payment payment = findPaymentById(paymentId);
        validatePaymentOwner(payment, userId);
        payment.failIfTimeout(); // 조회 시 지연시간 체크
        return toPaymentResponse(payment);
    }

    // 일반 결제 취소 (고객 통제)
    @Transactional
    public void cancelPayment(UUID paymentId, Long userId) {
        Payment payment = findPaymentById(paymentId);
        validatePaymentOwner(payment, userId);
        payment.failIfTimeout();

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED
                && payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "취소할 수 없는 상태입니다.");
        }

        // 결제 취소 시 원 주문 동기화 취소
        payment.cancel();
        if (payment.getOrder().getOrderStatus() != OrderStatus.CANCELED) {
            payment.getOrder().cancelByUser();
        }
    }

    // 내 결제 페이징 검색 (고객)
    public Page<PaymentSummaryResponse> searchPaymentsCustomer(Long userId, PaymentSearchCondition condition,
            Pageable pageable) {
        return paymentRepository
                .searchCustomerPayments(
                        userId,
                        condition.status(),
                        condition.startDate(),
                        condition.endDate(),
                        pageable)
                .map(this::toPaymentSummaryResponse);
    }

    // 가게 승인 거절 시 결제 강제 취소 (내부 서비스용)
    @Transactional
    public void cancelByOwnerReject(UUID paymentId) {
        Payment payment = findPaymentById(paymentId);
        payment.cancel();
    }

    // 가게 결제 내역 페이징 검색 (사장용)
    public Page<PaymentSummaryResponse> searchPaymentsOwner(UUID storeId, PaymentSearchCondition condition,
            Pageable pageable) {
        return paymentRepository.searchOwnerPayments(
                        storeId,
                        condition.status(),
                        condition.startDate(),
                        condition.endDate(),
                        pageable)
                .map(this::toPaymentSummaryResponse);
    }

    // 가게 결제 단건 상세 조회 (사장용)
    public PaymentResponse getPaymentOwner(UUID paymentId, UUID storeId) {
        Payment payment = findPaymentById(paymentId);
        // TODO: StoreId 유효성 체크 필요
        return toPaymentResponse(payment);
    }

    // 전체 결제 페이징 검색 (관리자용)
    public Page<PaymentSummaryResponse> searchPaymentsAdmin(PaymentSearchCondition condition, Pageable pageable) {
        return paymentRepository
                .searchAdminPayments(
                        condition.status(),
                        condition.startDate(),
                        condition.endDate(),
                        pageable)
                .map(this::toPaymentSummaryResponse);
    }

    // 관리자 결제 상세 조회
    public PaymentResponse getPaymentAdmin(UUID paymentId) {
        return toPaymentResponse(findPaymentById(paymentId));
    }

    // 헬퍼 메서드
    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "결제를 찾을 수 없습니다."));
    }

    private void validatePaymentOwner(Payment payment, Long userId) {
        if (!payment.getOrder().getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrder().getOrderId(),
                payment.getTotalAmount(),
                payment.getPaymentStatus(),
                payment.getCreatedAt());
    }

    private PaymentSummaryResponse toPaymentSummaryResponse(Payment payment) {
        return new PaymentSummaryResponse(
                payment.getPaymentId(),
                payment.getTotalAmount(),
                payment.getCreatedAt(),
                payment.getPaymentStatus(),
                payment.getPaymentMethod());
    }
}
