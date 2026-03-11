package nbcamp.food_order_platform.payment.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.payment.application.dto.query.PaymentSearchQuery;
import nbcamp.food_order_platform.payment.application.service.PaymentService;
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
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentService paymentService;

    // 전체 결제 페이징 검색 (관리자)
    @GetMapping
    public ResponseEntity<Page<PaymentSummaryResponse>> searchPayments(
            @AuthenticationPrincipal AuthUser authUser,
            PaymentSearchCondition condition,
            Pageable pageable) {
        PaymentSearchQuery query = new PaymentSearchQuery(condition.status(), condition.startDate(), condition.endDate());
        return ResponseEntity.ok(paymentService.searchPaymentsAdmin(query, pageable, authUser)
                .map(PaymentSummaryResponse::from));
    }

    // 전체 결제 상세 조회 (관리자)
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.getPaymentAdmin(paymentId, authUser)));
    }
}
