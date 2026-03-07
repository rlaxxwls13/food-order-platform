package nbcamp.food_order_platform.payment.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.payment.application.service.PaymentService;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentCreateRequest;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentSearchCondition;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentResponse;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 생성 대기 (고객)
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentCreateRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request, authUser.getUserId()));
    }

    // 결제 상세 조회 (고객)
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentCustomer(paymentId, authUser.getUserId()));
    }

    // 내 결제 페이징 검색 (고객)
    @GetMapping
    public ResponseEntity<Page<PaymentSummaryResponse>> searchPayments(
            @AuthenticationPrincipal AuthUser authUser,
            PaymentSearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.searchPaymentsCustomer(
                authUser.getUserId(),
                condition,
                pageable));
}

    // 내 결제 취소 (고객)
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID paymentId) {
        paymentService.cancelPayment(paymentId, authUser.getUserId());
        return ResponseEntity.ok().build();
    }
}
