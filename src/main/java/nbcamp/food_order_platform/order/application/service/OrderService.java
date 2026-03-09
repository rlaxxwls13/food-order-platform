package nbcamp.food_order_platform.order.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.order.application.dto.command.OrderCancelCommand;
import nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand;
import nbcamp.food_order_platform.order.application.dto.command.OrderItemCancelCommand;
import nbcamp.food_order_platform.order.application.dto.result.OrderAddressInfo;
import nbcamp.food_order_platform.order.application.dto.result.OrderItemInfo;
import nbcamp.food_order_platform.order.application.dto.result.OrderResult;
import nbcamp.food_order_platform.order.application.dto.result.OrderSummaryResult;
import nbcamp.food_order_platform.order.domain.entity.*;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 주문(Order) 도메인의 비즈니스 로직을 처리하는 서비스 클래스.
 *
 * <p>
 * 주요 기능:
 * </p>
 * <ul>
 * <li>주문 생성 (createOrder)</li>
 * <li>고객용 주문 단건 조회 (getOrderForCustomer)</li>
 * <li>관리자용 주문 단건 조회 (getOrderForAdmin)</li>
 * <li>고객용 주문 목록 조회 (getOrdersForCustomer)</li>
 * <li>관리자용 주문 목록 조회 (getOrdersForAdmin)</li>
 * <li>주문 전체 취소 (cancelOrder)</li>
 * <li>주문 상품 부분 취소 (cancelOrderItems)</li>
 * </ul>
 *
 * <p>
 * 모든 요청은 JWT 인증을 통해 전달된 사용자 정보(AuthUser)를 기반으로 처리됩니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // ========================================
    // 1. 주문 생성
    // ========================================

    /**
     * 새로운 주문을 생성합니다.
     *
     * <p>
     * 처리 흐름:
     * </p>
     * <ol>
     * <li>JWT에서 추출한 userId로 유저 존재 여부 검증</li>
     * <li>storeId로 가게 존재 여부 검증</li>
     * <li>주문 상품별로 Product 조회 및 재고 차감</li>
     * <li>Order 엔티티 생성 및 저장</li>
     * <li>OrderResult DTO로 변환하여 반환</li>
     * </ol>
     *
     * @param command 주문 생성에 필요한 정보 (storeId, items, addressId, userId)
     * @return 생성된 주문 정보를 담은 OrderResult
     */
    @Transactional
    public OrderResult createOrder(OrderCreateCommand command) {
        // 1. JWT에서 전달받은 userId로 유저 검증
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        // 2. 가게 존재 여부 검증
        Store store = storeRepository.findById(command.storeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        // 3. Order 엔티티 생성 (팩토리 메서드 활용)
        Order order = Order.createOrder(user, store, command);

        // 4. 저장
        Order savedOrder = orderRepository.save(order);

        // 5. 결과 DTO 변환 후 반환
        return toOrderResult(savedOrder);
    }

    // ========================================
    // 2. 주문 단건 조회 (고객용)
    // ========================================

    /**
     * 고객이 자신의 주문을 단건 조회합니다.
     *
     * <p>
     * JWT에서 추출한 userId와 주문의 소유자가 일치하는지 검증합니다.
     * </p>
     *
     * @param orderId 조회할 주문 ID
     * @param userId  JWT에서 추출한 현재 로그인 사용자 ID
     * @return 주문 상세 정보를 담은 OrderResult
     */
    public OrderResult getOrderForCustomer(UUID orderId, Long userId) {
        Order order = findOrderById(orderId);

        // 본인 주문인지 검증
        validateOrderOwner(order, userId);

        return toOrderResult(order);
    }

    // ========================================
    // 3. 주문 단건 조회 (관리자용)
    // ========================================

    /**
     * 관리자(MANAGER/MASTER)가 주문을 단건 조회합니다.
     *
     * <p>
     * 관리자는 모든 주문을 조회할 수 있으며, 유저 정보도 함께 반환됩니다.
     * </p>
     *
     * @param orderId 조회할 주문 ID
     * @return 유저 정보가 포함된 주문 상세 정보
     */
    public OrderResult getOrderForAdmin(UUID orderId) {
        Order order = findOrderById(orderId);
        return toOrderResultWithUserInfo(order);
    }

    // ========================================
    // 4. 주문 목록 조회 (고객용)
    // ========================================

    /**
     * 고객이 자신의 주문 목록을 조회합니다.
     *
     * <p>
     * JWT에서 추출한 userId를 기준으로 해당 유저의 모든 주문을 조회합니다.
     * </p>
     *
     * @param userId JWT에서 추출한 현재 로그인 사용자 ID
     * @return 주문 요약 목록 (OrderSummaryResult 리스트)
     */
    public List<OrderSummaryResult> getOrdersForCustomer(Long userId) {
        // 유저 존재 여부 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        return orderRepository.findAllByUserUserId(userId).stream()
                .map(this::toOrderSummaryResult)
                .collect(Collectors.toList());
    }

    // ========================================
    // 5. 주문 목록 조회 (관리자용)
    // ========================================

    /**
     * 관리자(MANAGER/MASTER)가 전체 주문 목록을 조회합니다.
     *
     * @return 전체 주문 요약 목록 (OrderSummaryResult 리스트)
     */
    public List<OrderSummaryResult> getOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .map(this::toOrderSummaryResult)
                .collect(Collectors.toList());
    }

    // ========================================
    // 6. 주문 전체 취소
    // ========================================

    /**
     * 주문을 전체 취소합니다.
     *
     * <p>
     * 결제 전(CREATED) 상태에서만 취소가 가능합니다.
     * </p>
     *
     * @param command 취소 요청 정보 (orderId, reason, requesterId)
     * @param userId  JWT에서 추출한 현재 로그인 사용자 ID
     */
    @Transactional
    public void cancelOrder(OrderCancelCommand command, Long userId) {
        Order order = findOrderById(command.orderId());

        // 본인 주문인지 검증
        validateOrderOwner(order, userId);

        // 주문 상태가 CREATED인지 검증 (결제 전에만 취소 가능)
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "결제 전 상태(CREATED)에서만 주문 취소가 가능합니다.");
        }

        // 주문 상태를 CANCELED로 변경
        order.cancel();
    }

    // ========================================
    // 7. 주문 상품 부분 취소
    // ========================================

    /**
     * 주문 내 특정 상품을 부분 취소합니다.
     *
     * <p>
     * Order 엔티티의 cancelOrderItem 메서드를 호출하여
     * 수량 차감 및 결제 금액 동기화를 처리합니다.
     * </p>
     *
     * @param command 부분 취소 요청 정보 (orderId, reason, items)
     * @param userId  JWT에서 추출한 현재 로그인 사용자 ID
     */
    @Transactional
    public void cancelOrderItems(OrderItemCancelCommand command, Long userId) {
        Order order = findOrderById(command.orderId());

        // 본인 주문인지 검증
        validateOrderOwner(order, userId);

        // 각 상품별 부분 취소 처리 (Order 엔티티의 도메인 로직 활용)
        for (OrderItemCancelCommand.ItemCancelDto item : command.items()) {
            order.cancelOrderItem(item.orderItemId(), item.quantity());
        }
    }

    // ========================================
    // 내부 헬퍼 메서드
    // ========================================

    /**
     * 주문 ID로 주문을 조회합니다.
     * 존재하지 않으면 NOT_EXISTED_ORDER 예외를 발생시킵니다.
     */
    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ORDER));
    }

    /**
     * 주문의 소유자(주문한 유저)와 현재 요청자가 동일한지 검증합니다.
     * 다를 경우 NO_PERMISSION 예외를 발생시킵니다.
     */
    private void validateOrderOwner(Order order, Long userId) {
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }

    /**
     * Order 엔티티를 OrderResult DTO로 변환합니다. (고객용 - 유저 정보 미포함)
     */
    private OrderResult toOrderResult(Order order) {
        return OrderResult.builder()
                .orderId(order.getOrderId())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getName())
                .status(order.getOrderStatus())
                .totalPrice(order.getTotalAmount())
                .items(toOrderItemInfoList(order.getOrderItems()))
                .address(toOrderAddressInfo(order.getSnapshotAddress()))
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * Order 엔티티를 OrderResult DTO로 변환합니다. (관리자용 - 유저 정보 포함)
     */
    private OrderResult toOrderResultWithUserInfo(Order order) {
        return OrderResult.builder()
                .orderId(order.getOrderId())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getName())
                .status(order.getOrderStatus())
                .totalPrice(order.getTotalAmount())
                .items(toOrderItemInfoList(order.getOrderItems()))
                .address(toOrderAddressInfo(order.getSnapshotAddress()))
                .createdAt(order.getCreatedAt())
                .userInfo(new OrderResult.UserInfo(
                        order.getUser().getUserId(),
                        order.getUser().getUsername()))
                .build();
    }

    /**
     * OrderItem 엔티티 목록을 OrderItemInfo DTO 목록으로 변환합니다.
     */
    private List<OrderItemInfo> toOrderItemInfoList(List<OrderItem> items) {
        return items.stream()
                .map(item -> OrderItemInfo.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .status(item.getOrderItemStatus())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * OrderAddress 엔티티를 OrderAddressInfo DTO로 변환합니다.
     */
    private OrderAddressInfo toOrderAddressInfo(OrderAddress address) {
        if (address == null)
            return null;
        return OrderAddressInfo.builder()
                .placeName(address.getPlaceName())
                .roadName(address.getRoadName())
                .detailName(address.getDetailName())
                .build();
    }

    /**
     * Order 엔티티를 OrderSummaryResult DTO로 변환합니다.
     * 대표 상품명은 첫 번째 상품명 + 나머지 상품 수로 구성됩니다.
     */
    private OrderSummaryResult toOrderSummaryResult(Order order) {
        // 대표 상품명 생성 (예: "후라이드 치킨 외 1건")
        String representativeItemName = "";
        if (!order.getOrderItems().isEmpty()) {
            representativeItemName = order.getOrderItems().get(0).getProductName();
            if (order.getOrderItems().size() > 1) {
                representativeItemName += " 외 " + (order.getOrderItems().size() - 1) + "건";
            }
        }

        return OrderSummaryResult.builder()
                .orderId(order.getOrderId())
                .storeName(order.getStore().getName())
                .representativeItemName(representativeItemName)
                .totalAmount(order.getTotalAmount())
                .status(order.getOrderStatus())
                .statusDescription(order.getOrderStatus().getDescription())
                .createdAt(order.getCreatedAt())
                .build();
    }
}