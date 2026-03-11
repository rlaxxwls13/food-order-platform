package nbcamp.food_order_platform.payment.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.payment.application.dto.command.PaymentCreateCommand;
import nbcamp.food_order_platform.payment.application.dto.query.PaymentSearchQuery;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentResult;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentSummaryResult;
import nbcamp.food_order_platform.payment.domain.entity.Payment;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import nbcamp.food_order_platform.payment.domain.repository.PaymentRepository;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import nbcamp.food_order_platform.global.security.AuthUser;
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
    private final StoreRepository storeRepository;

    // 결제 초기화 (READY 상태 생성)
    @Transactional
    public PaymentResult initiatePayment(PaymentCreateCommand command, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        validateSameUser(authUser, command.userId());
        Long userId = command.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ORDER));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "결제 가능한 주문 상태가 아닙니다.");
        }

        Payment payment = Payment.create(order, command.amount(), command.method());
        return toPaymentResult(paymentRepository.save(payment));
    }

    // 결제 성공 (웹훅 등에서 호출 시)
    @Transactional
    public PaymentResult completePayment(UUID paymentId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        Long userId = authUser.getUserId();

        Payment payment = findPaymentById(paymentId);
        validatePaymentOwner(payment, userId);
        payment.failIfTimeout();

        if (payment.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "결제가 15분을 초과하여 실패 처리되었습니다.");
        }

        payment.complete();
        payment.getOrder().updateStatus(OrderStatus.PAID);
        return toPaymentResult(payment);
    }

    // 결제 단건 조회 (고객용 - 지연시간 타임아웃 검증 포함)
    @Transactional
    public PaymentResult getPaymentCustomer(UUID paymentId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        Long userId = authUser.getUserId();

        Payment payment = findPaymentById(paymentId);
        validatePaymentOwner(payment, userId);
        payment.failIfTimeout(); // 조회 시 지연시간 체크
        return toPaymentResult(payment);
    }

    // 일반 결제 취소 (고객 통제)
    @Transactional
    public void cancelPayment(UUID paymentId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        Long userId = authUser.getUserId();

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
    public Page<PaymentSummaryResult> searchPaymentsCustomer(AuthUser authUser, PaymentSearchQuery query, Pageable pageable) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        Long userId = authUser.getUserId();

        return paymentRepository
                .searchCustomerPayments(
                        userId,
                        query.status(),
                        query.from(),
                        query.to(),
                        pageable)
                .map(this::toPaymentSummaryResult);
    }

    // 가게 승인 거절 시 결제 강제 취소 (내부 서비스용)
    @Transactional
    public void cancelByOwnerReject(UUID paymentId) {
        Payment payment = findPaymentById(paymentId);
        payment.cancel();
    }

    // 가게 결제 내역 페이징 검색 (사장용)
    public Page<PaymentSummaryResult> searchPaymentsOwner(UUID storeId, PaymentSearchQuery query,
            Pageable pageable, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateStorePermission(storeId, authUser.getUserId(), role);
        return paymentRepository.searchOwnerPayments(
                        storeId,
                        query.status(),
                        query.from(),
                        query.to(),
                        pageable)
                .map(this::toPaymentSummaryResult);
    }

    // 가게 결제 단건 상세 조회 (사장용)
    public PaymentResult getPaymentOwner(UUID paymentId, UUID storeId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateStorePermission(storeId, authUser.getUserId(), role);
        Payment payment = findPaymentById(paymentId);
        return toPaymentResult(payment);
    }

    // 전체 결제 페이징 검색 (관리자용)
    public Page<PaymentSummaryResult> searchPaymentsAdmin(PaymentSearchQuery query, Pageable pageable,
            AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateAdminPermission(role);
        return paymentRepository
                .searchAdminPayments(
                        query.status(),
                        query.from(),
                        query.to(),
                        pageable)
                .map(this::toPaymentSummaryResult);
    }

    // 관리자 결제 상세 조회
    public PaymentResult getPaymentAdmin(UUID paymentId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateAdminPermission(role);
        return toPaymentResult(findPaymentById(paymentId));
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

    private Role requireAuthUser(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.AUTHORIZATION);
        }
        try {
            return Role.valueOf(authUser.getRole());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_ROLE);
        }
    }

    private void validateCustomerPermission(Role role) {
        if (role != Role.CUSTOMER) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }

    private void validateSameUser(AuthUser authUser, Long commandUserId) {
        if (commandUserId == null || !commandUserId.equals(authUser.getUserId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }

    private void validateAdminPermission(Role role) {
        if (role != Role.MANAGER && role != Role.MASTER) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "관리자 권한이 없습니다.");
        }
    }

    public void validateStorePermission(UUID storeId, Long userId, Role role) { //가게 주인 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        if (role == Role.MANAGER || role == Role.MASTER)
            return;

        if (role == Role.OWNER && store.getOwnerId().equals(userId))
            return;

        throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
    }

    private PaymentResult toPaymentResult(Payment payment) {
        return PaymentResult.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .amount(payment.getTotalAmount())
                .status(payment.getPaymentStatus())
                .method(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .userInfo(new PaymentResult.UserInfo(payment.getOrder().getUser().getUserId(), payment.getOrder().getUser().getUsername()))
                .build();
    }

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
