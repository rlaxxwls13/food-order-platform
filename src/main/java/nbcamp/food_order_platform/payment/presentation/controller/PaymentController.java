package nbcamp.food_order_platform.payment.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.payment.application.dto.command.PaymentCancelCommand;
import nbcamp.food_order_platform.payment.application.dto.command.PaymentCreateCommand;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentResult;
import nbcamp.food_order_platform.payment.application.dto.result.PaymentSummaryResult;
import nbcamp.food_order_platform.payment.application.service.PaymentService;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentCreateRequest;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentCancelResponse;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentListResponse;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 결제(Payment) 도메인의 REST API 컨트롤러.
 *
 * <p>
 * 모든 요청은 JWT 인증이 필요하며, {@link AuthUser}를 통해 인증된 사용자 정보를 추출합니다.
 * </p>
 *
 * <p>
 * 주요 엔드포인트:
 * </p>
 * <ul>
 * <li>POST /api/v1/payments - 결제 생성</li>
 * <li>GET /api/v1/payments/{paymentId} - 결제 단건 조회</li>
 * <li>GET /api/v1/payments - 내 결제 목록 조회</li>
 * <li>POST /api/v1/payments/{paymentId}/cancel - 결제 취소</li>
 * <li>GET /api/v1/admin/payments - 전체 결제 목록 조회 (관리자)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ========================================
    // 고객(CUSTOMER) 전용 API
    // ========================================

    /**
     * 결제 생성 API.
     *
     * <p>
     * JWT에서 추출한 userId를 활용하여 주문에 대한 결제를 진행합니다.
     * 클라이언트는 orderId, 결제 수단(method), 결제 금액(amount)을 전달합니다.
     * </p>
     *
     * <p>
     * 결제가 성공하면 주문 상태가 CREATED → PAID로 변경됩니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보
     * @param request  결제 생성 요청 DTO (orderId, method, amount)
     * @return 생성된 결제 정보 (201 CREATED)
     */
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentCreateRequest request) {

        // Presentation DTO → Application Command 변환 (userId는 JWT에서 추출)
        PaymentCreateCommand command = new PaymentCreateCommand(
                request.orderId(),
                request.method(),
                request.amount(),
                authUser.getUserId());

        // 서비스 호출
        PaymentResult result = paymentService.createPayment(command);

        // Application Result → Presentation Response 변환
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(result.paymentId())
                .orderId(result.orderId())
                .amount(result.amount())
                .status(result.status().getDescription())
                .createdAt(result.createdAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 결제 단건 조회 API.
     *
     * <p>
     * JWT에서 추출한 userId와 결제의 소유자(주문자)가 일치하는 경우에만 조회 가능합니다.
     * </p>
     *
     * @param authUser  JWT에서 추출한 인증된 사용자 정보
     * @param paymentId 조회할 결제 ID
     * @return 결제 상세 정보
     */
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID paymentId) {

        PaymentResult result = paymentService.getPayment(paymentId, authUser.getUserId());

        // Application Result → Presentation Response 변환
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(result.paymentId())
                .orderId(result.orderId())
                .amount(result.amount())
                .status(result.status().getDescription())
                .createdAt(result.createdAt())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 내 결제 목록 조회 API.
     *
     * <p>
     * JWT에서 추출한 userId 기반으로 해당 유저의 전체 결제 내역을 조회합니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보
     * @return 결제 요약 목록
     */
    @GetMapping("/payments")
    public ResponseEntity<PaymentListResponse> getMyPayments(
            @AuthenticationPrincipal AuthUser authUser) {

        List<PaymentSummaryResult> results = paymentService.getPaymentsByUser(authUser.getUserId());

        return ResponseEntity.ok(new PaymentListResponse(results));
    }

    /**
     * 결제 취소 API.
     *
     * <p>
     * 결제 완료(COMPLETED) 상태에서만 취소가 가능합니다.
     * 취소 시 결제 상태가 CANCELLED로 변경되고, 전체 금액이 환불 처리됩니다.
     * </p>
     *
     * @param authUser  JWT에서 추출한 인증된 사용자 정보
     * @param paymentId 취소할 결제 ID
     * @return 취소된 결제 정보 (200 OK)
     */
    @PostMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<PaymentCancelResponse> cancelPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID paymentId) {

        // 결제 취소 Command 생성
        PaymentCancelCommand command = new PaymentCancelCommand(
                paymentId,
                "사용자 요청에 의한 결제 취소",
                null);

        paymentService.cancelPayment(command, authUser.getUserId());

        return ResponseEntity.ok(new PaymentCancelResponse(
                paymentId,
                "결제 취소 완료"));
    }

    // ========================================
    // 관리자(ADMIN) 전용 API
    // ========================================

    /**
     * 관리자용 전체 결제 목록 조회 API.
     *
     * <p>
     * 관리자(MANAGER/MASTER)는 시스템의 전체 결제 내역을 조회할 수 있습니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보 (관리자 권한 필요)
     * @return 전체 결제 요약 목록
     */
    @GetMapping("/admin/payments")
    public ResponseEntity<PaymentListResponse> getAllPayments(
            @AuthenticationPrincipal AuthUser authUser) {

        List<PaymentSummaryResult> results = paymentService.getAllPayments();

        return ResponseEntity.ok(new PaymentListResponse(results));
    }
}
