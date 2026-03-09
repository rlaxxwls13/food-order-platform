package nbcamp.food_order_platform.payment.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.payment.application.service.PaymentService;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentSearchCondition;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentResponse;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentService paymentService;

    // 전체 결제 페이징 검색 (관리자)
    @GetMapping
    public ResponseEntity<Page<PaymentSummaryResponse>> searchPayments(
            PaymentSearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.searchPaymentsAdmin(condition, pageable));
    }

    // 전체 결제 상세 조회 (관리자)
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentAdmin(paymentId));
    }
}
