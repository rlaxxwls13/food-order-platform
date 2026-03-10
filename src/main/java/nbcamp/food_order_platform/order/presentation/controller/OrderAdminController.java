package nbcamp.food_order_platform.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.dto.query.OrderSearchQuery;
import nbcamp.food_order_platform.order.application.service.OrderService;
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
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    // 주문 페이징 검색 (관리자)
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> searchOrders(
            @AuthenticationPrincipal AuthUser authUser,
            OrderSearchCondition condition,
            Pageable pageable) {
        OrderSearchQuery query = new OrderSearchQuery(
                null,
                null,
                condition.status(),
                condition.startDate(),
                condition.endDate()
        );

        return ResponseEntity.ok(orderService.searchOrdersAdmin(query, pageable, authUser)
                .map(OrderSummaryResponse::from));
    }

    //주문 상세 조회 (관리자)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getOrderAdmin(orderId, authUser)));
    }

    // 주문 강제 취소 (관리자)
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        orderService.cancelOrderByAdmin(orderId, authUser);
        return ResponseEntity.ok().build();
    }
}
