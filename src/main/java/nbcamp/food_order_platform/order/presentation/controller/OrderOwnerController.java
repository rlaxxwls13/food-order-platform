package nbcamp.food_order_platform.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.dto.command.OrderRejectCommand;
import nbcamp.food_order_platform.order.application.dto.query.OrderSearchQuery;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderRejectRequest;
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
@RequestMapping("/api/v1/owner/orders")
@RequiredArgsConstructor
public class OrderOwnerController {

    private final OrderService orderService;

    // 가게 주문 페이징 검색 (사장)
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> searchOrders(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam UUID storeId,
            OrderSearchCondition condition,
            Pageable pageable) {
        OrderSearchQuery query = new OrderSearchQuery(
                null,
                storeId,
                condition.status(),
                condition.startDate(),
                condition.endDate()
        );

        return ResponseEntity.ok(orderService.searchOrdersOwner(query, pageable, authUser)
                .map(OrderSummaryResponse::from));
    }

    // 가게 주문 상세 조회 (사장)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getOrderOwner(orderId, authUser)));
    }

    // 주문 승인 (사장)
    @PostMapping("/{orderId}/accept")
    public ResponseEntity<Void> acceptOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        orderService.acceptOrderByOwner(orderId, authUser);
        return ResponseEntity.ok().build();
    }

    // 주문 거절 (사장)
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<Void> rejectOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderRejectRequest request) {
        orderService.rejectOrderByOwner(orderId, new OrderRejectCommand(request.reason()), authUser);
        return ResponseEntity.ok().build();
    }
}
