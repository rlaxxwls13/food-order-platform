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
@RequestMapping("/api/v1/owner/payments")
@RequiredArgsConstructor
public class PaymentOwnerController {

    private final PaymentService paymentService;

    // 가게 결제 내역 페이징 검색 (사장)
    @GetMapping
    public ResponseEntity<Page<PaymentSummaryResponse>> searchPayments(
            @RequestParam UUID storeId,
            PaymentSearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.searchPaymentsOwner(storeId, condition, pageable));
    }

    // 결제 상세 조회 (사장)
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId,
            @RequestParam UUID storeId) {
        return ResponseEntity.ok(paymentService.getPaymentOwner(paymentId, storeId));
    }
}
