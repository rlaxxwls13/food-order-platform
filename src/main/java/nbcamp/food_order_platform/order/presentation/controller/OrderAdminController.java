package nbcamp.food_order_platform.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderSearchCondition;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderResponse;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    // 전체 주문 페이징 검색 (관리자)
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> searchOrders(
            OrderSearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.searchOrdersAdmin(condition, pageable));
    }

    // 전체 주문 상세 조회 (관리자)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderAdmin(orderId));
    }

    // 전체 주문 강제 취소 (관리자)
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelOrderByAdmin(orderId);
        return ResponseEntity.ok().build();
    }
}
