package nbcamp.food_order_platform.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand;
import nbcamp.food_order_platform.order.application.dto.query.OrderSearchQuery;
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

import java.util.stream.Collectors;
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
        OrderCreateCommand command = new OrderCreateCommand(
                request.storeId(),
                request.comment(),
                request.items().stream()
                        .map(i -> new OrderCreateCommand.OrderItemCommand(i.productId(), i.quantity()))
                        .collect(Collectors.toList()),
                request.addressId(),
                authUser.getUserId()
        );

        return ResponseEntity.ok(OrderResponse.from(orderService.createOrder(command, authUser)));
    }

    // 내 주문 상세 조회 (고객)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getOrderCustomer(orderId, authUser)));
    }

    // 내 주문 내역 검색/페이징 조회 (고객)
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> searchOrders(
            @AuthenticationPrincipal AuthUser authUser,
            OrderSearchCondition condition,
            Pageable pageable) {
        OrderSearchQuery query = new OrderSearchQuery(
                authUser.getUserId(),
                null,
                condition.status(),
                condition.startDate(),
                condition.endDate()
        );

        return ResponseEntity.ok(orderService.searchOrdersCustomer(authUser, query, pageable)
                .map(OrderSummaryResponse::from));
    }

    // 내 주문 취소 (고객)
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {
        orderService.cancelOrderByUser(orderId, authUser);
        return ResponseEntity.ok().build();
    }
}
