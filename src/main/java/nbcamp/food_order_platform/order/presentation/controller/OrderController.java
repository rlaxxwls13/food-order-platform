package nbcamp.food_order_platform.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderCreateRequest;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderSearchCondition;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderResponse;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 생성 (고객)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request, authUser.getUserId()));
    }

    // 내 주문 상세 조회 (고객)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderCustomer(orderId, authUser.getUserId()));
    }

    // 내 주문 내역 검색/페이징 조회 (고객)
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> searchOrders(
            @AuthenticationPrincipal AuthUser authUser,
            OrderSearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.searchOrdersCustomer(authUser.getUserId(), condition, pageable));
    }

    // 내 주문 취소 (고객)
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        orderService.cancelOrderByUser(orderId, authUser.getUserId());
        return ResponseEntity.ok().build();
    }
}
