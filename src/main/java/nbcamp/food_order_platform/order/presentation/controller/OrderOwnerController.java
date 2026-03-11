package nbcamp.food_order_platform.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderRejectRequest;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderSearchCondition;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderResponse;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner/orders")
@RequiredArgsConstructor
public class OrderOwnerController {

    private final OrderService orderService;

    // 가게 주문 페이징 검색 (사장)
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> searchOrders(
            @RequestParam UUID storeId,
            OrderSearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.searchOrdersOwner(storeId, condition, pageable));
    }

    // 가게 주문 상세 조회 (사장)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderOwner(orderId));
    }

    // 주문 승인 (사장)
    @PostMapping("/{orderId}/accept")
    public ResponseEntity<Void> acceptOrder(@PathVariable UUID orderId) {
        orderService.acceptOrderByOwner(orderId);
        return ResponseEntity.ok().build();
    }

    // 주문 거절 (사장)
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<Void> rejectOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderRejectRequest request) {
        orderService.rejectOrderByOwner(orderId, request);
        return ResponseEntity.ok().build();
    }
}
