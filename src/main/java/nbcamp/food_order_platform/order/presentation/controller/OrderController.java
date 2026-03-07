package nbcamp.food_order_platform.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.dto.command.OrderCancelCommand;
import nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand;
import nbcamp.food_order_platform.order.application.dto.command.OrderItemCancelCommand;
import nbcamp.food_order_platform.order.application.dto.result.OrderResult;
import nbcamp.food_order_platform.order.application.dto.result.OrderSummaryResult;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.presentation.dto.request.*;
import nbcamp.food_order_platform.order.presentation.dto.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 주문(Order) 도메인의 REST API 컨트롤러.
 *
 * <p>
 * 모든 요청은 JWT 인증이 필요하며, {@link AuthUser}를 통해 인증된 사용자 정보를 추출합니다.
 * </p>
 *
 * <p>
 * 주요 엔드포인트:
 * </p>
 * <ul>
 * <li>POST /api/v1/orders - 주문 생성 (CUSTOMER)</li>
 * <li>GET /api/v1/orders/{orderId} - 주문 단건 조회 (CUSTOMER)</li>
 * <li>GET /api/v1/orders - 주문 목록 조회 (CUSTOMER)</li>
 * <li>POST /api/v1/orders/{orderId}/cancel - 주문 전체 취소 (CUSTOMER)</li>
 * <li>POST /api/v1/orders/{orderId}/items/cancel - 주문 상품 부분 취소 (CUSTOMER)</li>
 * <li>GET /api/v1/admin/orders/{orderId} - 주문 단건 조회 (ADMIN)</li>
 * <li>GET /api/v1/admin/orders - 주문 목록 조회 (ADMIN)</li>
 * <li>POST /api/v1/admin/orders/{orderId}/cancel - 주문 취소 (ADMIN)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ========================================
    // 고객(CUSTOMER) 전용 API
    // ========================================

    /**
     * 주문 생성 API.
     *
     * <p>
     * JWT에서 추출한 userId를 활용하여 주문을 생성합니다.
     * 클라이언트는 storeId, items(상품목록), addressId를 전달합니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보
     * @param request  주문 생성 요청 DTO (storeId, comment, items, addressId)
     * @return 생성된 주문 정보 (201 CREATED)
     */
    @PostMapping("/orders")
    public ResponseEntity<PostOrderCustomerResDto> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateOrderReqDto request) {

        // Presentation DTO → Application Command 변환 (userId는 JWT에서 추출)
        OrderCreateCommand command = new OrderCreateCommand(
                request.storeId(),
                request.comment(),
                request.items().stream()
                        .map(item -> new OrderCreateCommand.OrderItemCommand(
                                item.productId(), item.quantity()))
                        .collect(Collectors.toList()),
                request.addressId(),
                authUser.getUserId());

        // 서비스 호출
        OrderResult result = orderService.createOrder(command);

        // Application Result → Presentation Response 변환
        PostOrderCustomerResDto response = PostOrderCustomerResDto.builder()
                .orderId(result.orderId())
                .storeId(result.storeId())
                .status(result.status())
                .totalPrice(result.totalPrice())
                .items(toOrderItemResDtoList(result))
                .address(toOrderAddressResDto(result))
                .createdAt(result.createdAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 고객용 주문 단건 조회 API.
     *
     * <p>
     * JWT에서 추출한 userId와 주문 소유자가 일치하는 경우에만 조회 가능합니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보
     * @param orderId  조회할 주문 ID
     * @return 주문 상세 정보
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<GetOrderCustomerResDto> getOrderForCustomer(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {

        OrderResult result = orderService.getOrderForCustomer(orderId, authUser.getUserId());

        // Application Result → Presentation Response 변환
        GetOrderCustomerResDto response = GetOrderCustomerResDto.builder()
                .orderId(result.orderId())
                .userName(authUser.getUsername())
                .storeName(result.storeName())
                .status(result.status())
                .totalPrice(result.totalPrice())
                .items(toOrderItemResDtoList(result))
                .address(toOrderAddressResDto(result))
                .createdAt(result.createdAt())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 고객용 주문 목록 조회 API.
     *
     * <p>
     * JWT에서 추출한 userId 기반으로 해당 고객의 전체 주문 내역을 조회합니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보
     * @return 주문 요약 목록
     */
    @GetMapping("/orders")
    public ResponseEntity<GetOrdersCustomerResDto> getOrdersForCustomer(
            @AuthenticationPrincipal AuthUser authUser) {

        List<OrderSummaryResult> results = orderService.getOrdersForCustomer(authUser.getUserId());

        // Application Result → Presentation Response 변환
        List<OrderSummaryCustomerDto> summaries = results.stream()
                .map(r -> OrderSummaryCustomerDto.builder()
                        .orderId(r.orderId())
                        .storeName(r.storeName())
                        .representativeItemName(r.representativeItemName())
                        .totalAmount(r.totalAmount())
                        .status(r.status())
                        .statusDescription(r.statusDescription())
                        .createdAt(r.createdAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new GetOrdersCustomerResDto(summaries));
    }

    /**
     * 고객용 주문 전체 취소 API.
     *
     * <p>
     * 결제 전(CREATED) 상태에서만 취소가 가능합니다.
     * 취소 사유를 함께 전달해야 합니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보
     * @param orderId  취소할 주문 ID
     * @param request  취소 사유가 포함된 요청 DTO
     * @return 취소된 주문 ID (200 OK)
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderCancelResponse> cancelOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody PostOrderCancelCustomerReqDto request) {

        // Presentation DTO → Application Command 변환
        OrderCancelCommand command = new OrderCancelCommand(
                orderId,
                request.reason(),
                null // requesterId는 orderId 기반 조회로 대체
        );

        orderService.cancelOrder(command, authUser.getUserId());

        return ResponseEntity.ok(OrderCancelResponse.builder()
                .orderId(orderId)
                .build());
    }

    /**
     * 고객용 주문 상품 부분 취소 API.
     *
     * <p>
     * 주문 내 특정 상품의 수량을 부분적으로 취소합니다.
     * 수량이 0이 되면 해당 상품은 전체 취소(CANCELED) 상태로 변경됩니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보
     * @param orderId  주문 ID
     * @param request  부분 취소할 상품 목록 및 사유
     * @return 처리된 주문 ID (200 OK)
     */
    @PostMapping("/orders/{orderId}/items/cancel")
    public ResponseEntity<OrderCancelResponse> cancelOrderItems(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody PostOrderItemCancelCustomerReqDto request) {

        // Presentation DTO → Application Command 변환
        List<OrderItemCancelCommand.ItemCancelDto> items = request.item().stream()
                .map(dto -> new OrderItemCancelCommand.ItemCancelDto(
                        dto.orderItemId(), dto.quantity()))
                .collect(Collectors.toList());

        OrderItemCancelCommand command = new OrderItemCancelCommand(
                orderId,
                request.reason(),
                items);

        orderService.cancelOrderItems(command, authUser.getUserId());

        return ResponseEntity.ok(OrderCancelResponse.builder()
                .orderId(orderId)
                .build());
    }

    // ========================================
    // 관리자(ADMIN) 전용 API
    // ========================================

    /**
     * 관리자용 주문 단건 조회 API.
     *
     * <p>
     * 관리자(MANAGER/MASTER)는 모든 주문을 조회할 수 있으며,
     * 주문한 유저 정보도 함께 반환됩니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보 (관리자 권한 필요)
     * @param orderId  조회할 주문 ID
     * @return 관리자용 주문 상세 정보
     */
    @GetMapping("/admin/orders/{orderId}")
    public ResponseEntity<GetOrderAdminResDto> getOrderForAdmin(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId) {

        OrderResult result = orderService.getOrderForAdmin(orderId);

        GetOrderAdminResDto response = GetOrderAdminResDto.builder()
                .orderId(result.orderId())
                .storeId(result.storeId())
                .address(toOrderAddressResDto(result))
                .Items(toOrderItemResDtoList(result))
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 관리자용 주문 목록 조회 API.
     *
     * <p>
     * 관리자(MANAGER/MASTER)는 시스템의 전체 주문을 조회할 수 있습니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보 (관리자 권한 필요)
     * @return 전체 주문 요약 목록
     */
    @GetMapping("/admin/orders")
    public ResponseEntity<GetOrdersAdminResDto> getOrdersForAdmin(
            @AuthenticationPrincipal AuthUser authUser) {

        List<OrderSummaryResult> results = orderService.getOrdersForAdmin();

        List<OrderSummaryAdminDto> summaries = results.stream()
                .map(r -> OrderSummaryAdminDto.builder()
                        .orderId(r.orderId())
                        .storeName(r.storeName())
                        .representativeItemName(r.representativeItemName())
                        .totalAmount(r.totalAmount())
                        .status(r.status())
                        .statusDescription(r.statusDescription())
                        .createdAt(r.createdAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new GetOrdersAdminResDto(summaries));
    }

    /**
     * 관리자용 주문 취소 API.
     *
     * <p>
     * 관리자(MANAGER/MASTER)가 관리 목적으로 주문을 강제 취소합니다.
     * </p>
     *
     * @param authUser JWT에서 추출한 인증된 사용자 정보 (관리자 권한 필요)
     * @param orderId  취소할 주문 ID
     * @param request  취소 사유가 포함된 요청 DTO
     * @return 취소된 주문 ID (200 OK)
     */
    @PostMapping("/admin/orders/{orderId}/cancel")
    public ResponseEntity<PostOrderCancelAdminResDto> cancelOrderForAdmin(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderCancelRequest request) {

        OrderCancelCommand command = new OrderCancelCommand(
                orderId,
                request.reason(),
                null);

        // 관리자 취소는 소유자 검증 없이 처리 (별도 관리자 전용 메서드 호출 가능)
        orderService.cancelOrder(command, null);

        return ResponseEntity.ok(PostOrderCancelAdminResDto.builder()
                .orderId(orderId)
                .build());
    }

    // ========================================
    // 내부 변환 헬퍼 메서드
    // ========================================

    /**
     * OrderResult의 items를 OrderItemResDto 목록으로 변환합니다.
     */
    private List<OrderItemResDto> toOrderItemResDtoList(OrderResult result) {
        if (result.items() == null)
            return List.of();
        return result.items().stream()
                .map(item -> OrderItemResDto.builder()
                        .productId(item.productId())
                        .productName(item.productName())
                        .price(item.price())
                        .quantity(item.quantity())
                        .status(item.status())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * OrderResult의 address를 OrderAddressResDto로 변환합니다.
     */
    private OrderAddressResDto toOrderAddressResDto(OrderResult result) {
        if (result.address() == null)
            return null;
        return OrderAddressResDto.builder()
                .placeName(result.address().placeName())
                .roadName(result.address().roadName())
                .detailName(result.address().detailName())
                .build();
    }
}