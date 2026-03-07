package nbcamp.food_order_platform.payment.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.payment.application.dto.command.PaymentCancelCommand;
import nbcamp.food_order_platform.payment.application.dto.command.PaymentCreateCommand;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentResult;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentSummaryResult;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import nbcamp.food_order_platform.payment.domain.repository.PaymentRepository;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 결제(Payment) 도메인의 비즈니스 로직을 처리하는 서비스 클래스.
 *
 * <p>
 * 주요 기능:
 * </p>
 * <ul>
 * <li>결제 생성 (createPayment) - 주문에 대한 결제를 처리</li>
 * <li>결제 단건 조회 (getPayment) - 특정 결제 정보 조회</li>
 * <li>결제 목록 조회 (getPaymentsByUser) - 유저별 결제 내역 조회</li>
 * <li>결제 취소 (cancelPayment) - 결제된 건에 대한 취소 처리</li>
 * </ul>
 *
 * <p>
 * 모든 요청은 JWT 인증을 통해 전달된 사용자 정보(AuthUser)를 기반으로 처리됩니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // ========================================
    // 1. 결제 생성
    // ========================================

    /**
     * 새로운 결제를 생성합니다.
     *
     * <p>
     * 처리 흐름:
     * </p>
     * <ol>
     * <li>JWT에서 추출한 userId로 유저 존재 여부 검증</li>
     * <li>orderId로 주문 존재 여부 검증</li>
     * <li>주문의 소유자가 요청자와 일치하는지 검증</li>
     * <li>주문 상태가 CREATED(결제 대기)인지 검증</li>
     * <li>결제 금액이 주문 총액과 일치하는지 검증</li>
     * <li>Payment 엔티티 생성 및 저장</li>
     * <li>주문 상태를 PAID로 변경</li>
     * </ol>
     *
     * @param command 결제 생성에 필요한 정보 (orderId, method, amount, userId)
     * @return 생성된 결제 정보를 담은 PaymentResult
     */
    @Transactional
    public PaymentResult createPayment(PaymentCreateCommand command) {
        // 1. JWT에서 전달받은 userId로 유저 검증
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        // 2. 주문 존재 여부 검증
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ORDER));

        // 3. 주문의 소유자가 현재 요청자인지 검증
        if (!order.getUser().getUserId().equals(command.userId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        // 4. 주문 상태가 CREATED(결제 대기)인지 검증
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "결제 가능한 주문 상태(CREATED)가 아닙니다. 현재 상태: " + order.getOrderStatus().getDescription());
        }

        // 5. 결제 금액과 주문 총액이 일치하는지 검증
        if (!order.getTotalAmount().equals(command.amount())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "결제 금액이 주문 총액과 일치하지 않습니다. 주문 총액: " + order.getTotalAmount());
        }

        // 6. Payment 엔티티 생성 및 저장
        Payment payment = Payment.create(order, command.amount(), command.method());
        Payment savedPayment = paymentRepository.save(payment);

        // 7. 주문 상태를 PAID로 변경
        order.updateStatus(OrderStatus.PAID);

        // 8. 결과 DTO 변환 후 반환
        return toPaymentResult(savedPayment);
    }

    // ========================================
    // 2. 결제 단건 조회
    // ========================================

    /**
     * 결제를 단건 조회합니다.
     *
     * <p>
     * JWT에서 추출한 userId와 결제 관련 주문의 소유자가 일치하는지 검증합니다.
     * </p>
     *
     * @param paymentId 조회할 결제 ID
     * @param userId    JWT에서 추출한 현재 로그인 사용자 ID
     * @return 결제 상세 정보를 담은 PaymentResult
     */
    public PaymentResult getPayment(UUID paymentId, Long userId) {
        Payment payment = findPaymentById(paymentId);

        // 본인 결제인지 검증 (결제에 연결된 주문의 소유자 확인)
        validatePaymentOwner(payment, userId);

        return toPaymentResult(payment);
    }

    // ========================================
    // 3. 결제 목록 조회 (유저별)
    // ========================================

    /**
     * 특정 유저의 결제 목록을 조회합니다.
     *
     * <p>
     * JWT에서 추출한 userId를 기반으로 해당 유저와 관련된 모든 결제를 조회합니다.
     * </p>
     *
     * @param userId JWT에서 추출한 현재 로그인 사용자 ID
     * @return 결제 요약 목록 (PaymentSummaryResult 리스트)
     */
    public List<PaymentSummaryResult> getPaymentsByUser(Long userId) {
        // 유저 존재 여부 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        return paymentRepository.findAllByOrderUserUserId(userId).stream()
                .map(this::toPaymentSummaryResult)
                .collect(Collectors.toList());
    }

    // ========================================
    // 4. 결제 취소
    // ========================================

    /**
     * 결제를 취소합니다.
     *
     * <p>
     * 처리 흐름:
     * </p>
     * <ol>
     * <li>결제 존재 여부 검증</li>
     * <li>본인 결제인지 검증</li>
     * <li>결제 상태가 COMPLETED(성공)인지 검증</li>
     * <li>결제 상태를 CANCELLED로 변경</li>
     * <li>취소 금액을 전체 결제 금액으로 설정</li>
     * </ol>
     *
     * @param command 결제 취소 정보 (paymentId, reason, requesterId)
     * @param userId  JWT에서 추출한 현재 로그인 사용자 ID
     */
    @Transactional
    public void cancelPayment(PaymentCancelCommand command, Long userId) {
        Payment payment = findPaymentById(command.paymentId());

        // 본인 결제인지 검증
        validatePaymentOwner(payment, userId);

        // 결제 상태가 COMPLETED인지 검증 (성공한 결제만 취소 가능)
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "결제 완료(COMPLETED) 상태에서만 취소가 가능합니다. 현재 상태: " + payment.getPaymentStatus().getDescription());
        }

        // 결제 취소 처리
        payment.cancel();
    }

    // ========================================
    // 관리자 전용: 결제 목록 전체 조회
    // ========================================

    /**
     * 관리자(MANAGER/MASTER)가 전체 결제 목록을 조회합니다.
     *
     * @return 전체 결제 요약 목록 (PaymentSummaryResult 리스트)
     */
    public List<PaymentSummaryResult> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentSummaryResult)
                .collect(Collectors.toList());
    }

    // ========================================
    // 내부 헬퍼 메서드
    // ========================================

    /**
     * 결제 ID로 결제를 조회합니다.
     * 존재하지 않으면 NOT_EXISTED_ORDER 예외를 발생시킵니다.
     */
    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED,
                        "존재하지 않는 결제입니다."));
    }

    /**
     * 결제 소유자(주문한 유저)와 현재 요청자가 동일한지 검증합니다.
     * 다를 경우 NO_PERMISSION 예외를 발생시킵니다.
     */
    private void validatePaymentOwner(Payment payment, Long userId) {
        if (!payment.getOrder().getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }

    /**
     * Payment 엔티티를 PaymentResult DTO로 변환합니다.
     */
    private PaymentResult toPaymentResult(Payment payment) {
        return PaymentResult.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .amount(payment.getTotalAmount())
                .status(payment.getPaymentStatus())
                .method(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * Payment 엔티티를 PaymentSummaryResult DTO로 변환합니다.
     */
    private PaymentSummaryResult toPaymentSummaryResult(Payment payment) {
        return PaymentSummaryResult.builder()
                .paymentId(payment.getPaymentId())
                .amount(payment.getTotalAmount())
                .createdAt(payment.getCreatedAt())
                .status(payment.getPaymentStatus())
                .method(payment.getPaymentMethod())
                .build();
    }
}
