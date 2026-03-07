package nbcamp.food_order_platform.order.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand;
import nbcamp.food_order_platform.order.domain.entity.*;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderCreateRequest;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderRejectRequest;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderSearchCondition;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderResponse;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderSummaryResponse;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Address;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.AddressRepository;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

    // 주문 생성
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "존재하지 않는 주소입니다."));
        OrderAddress snapshotAddress = new OrderAddress(
                address.getPlaceName(),
                address.getRoadName(),
                address.getDetailName());

        List<OrderCreateCommand.OrderItemCommand> commandItems = request.items().stream()
                .map(item -> new OrderCreateCommand.OrderItemCommand(item.productId(), item.quantity()))
                .collect(Collectors.toList());

        OrderCreateCommand mockCommand = new OrderCreateCommand(
                request.storeId(),
                request.comment(),
                commandItems,
                request.addressId(),
                userId);

        Order order = Order.createOrder(user, store, mockCommand);
        order.setSnapshotAddress(snapshotAddress);

        for (OrderCreateRequest.OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));
            product.decreaseStock(itemReq.quantity().intValue());

            OrderItem orderItem = OrderItem.create(product, itemReq.quantity());
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        return toOrderResponse(savedOrder);
    }

    // 내 주문 상세 조회 (고객)
    public OrderResponse getOrderCustomer(UUID orderId, Long userId) {
        Order order = findOrderById(orderId);
        validateOrderOwner(order, userId);
        return toOrderResponse(order);
    }

    // 가게 주문 상세 조회 (사장)
    public OrderResponse getOrderOwner(UUID orderId) {
        return toOrderResponse(findOrderById(orderId));
    }

    // 전체 주문 상세 조회 (관리자)
    public OrderResponse getOrderAdmin(UUID orderId) {
        return toOrderResponse(findOrderById(orderId));
    }

    // 내 주문 페이징 검색 (고객)
    public Page<OrderSummaryResponse> searchOrdersCustomer(Long userId, OrderSearchCondition condition,
            Pageable pageable) {
        return orderRepository.searchCustomerOrders(
                        userId,
                        condition.status(),
                        condition.startDate(),
                        condition.endDate(),
                        pageable)
                .map(this::toOrderSummaryResponse);
    }
                
    // 가게 주문 페이징 검색 (사장)
    public Page<OrderSummaryResponse> searchOrdersOwner(UUID storeId, OrderSearchCondition condition,
            Pageable pageable) {
        return orderRepository.searchOwnerOrders(
                        storeId,
                        condition.status(),
                        condition.startDate(),
                        condition.endDate(),
                        pageable)
                .map(this::toOrderSummaryResponse);
    }
    // 전체 주문 페이징 검색 (관리자)
    public Page<OrderSummaryResponse> searchOrdersAdmin(OrderSearchCondition condition, Pageable pageable) {
        return orderRepository
                .searchAdminOrders(condition.status(), condition.startDate(), condition.endDate(), pageable)
                .map(this::toOrderSummaryResponse);
    }

    @Transactional
    public void cancelOrderByUser(UUID orderId, Long userId) {
        Order order = findOrderById(orderId);
        validateOrderOwner(order, userId);
        order.cancelByUser();
        if (order.getPayment() != null) {
            order.getPayment().cancel();
        }
    }

    // 사장 주문 승인
    @Transactional
    public void acceptOrderByOwner(UUID orderId) {
        Order order = findOrderById(orderId);
        order.acceptByOwner();
    }

    // 사장 주문 거절 및 환불
    @Transactional
    public void rejectOrderByOwner(UUID orderId, OrderRejectRequest request) {
        Order order = findOrderById(orderId);
        order.rejectByOwner();
    }

    // 관리자 주문 강제 취소
    @Transactional
    public void cancelOrderByAdmin(UUID orderId) {
        Order order = findOrderById(orderId);
        order.cancelByAdmin();
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ORDER));
    }

    private void validateOrderOwner(Order order, Long userId) {
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }
    // --- DTO 매핑 ---
    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getName())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .orderItems(order.getOrderItems().stream().map(this::toOrderItemResponse).collect(Collectors.toList()))
                .snapshotAddress(toOrderAddressResponse(order.getSnapshotAddress()))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .orderItemStatus(item.getOrderItemStatus().name())
                .build();
    }

    private OrderResponse.OrderAddressResponse toOrderAddressResponse(OrderAddress address) {
        if (address == null)
            return null;
        return OrderResponse.OrderAddressResponse.builder()
                .placeName(address.getPlaceName())
                .roadName(address.getRoadName())
                .detailName(address.getDetailName())
                .build();
    }

    private OrderSummaryResponse toOrderSummaryResponse(Order order) {
        String repName = order.getOrderItems().isEmpty() ? "" : order.getOrderItems().get(0).getProductName();
        if (order.getOrderItems().size() > 1) {
            repName += " 외 " + (order.getOrderItems().size() - 1) + "건";
        }
        return OrderSummaryResponse.builder()
                .orderId(order.getOrderId())
                .storeName(order.getStore().getName())
                .representativeItemName(repName)
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .statusDescription(order.getOrderStatus().getDescription())
                .createdAt(order.getCreatedAt())
                .build();
    }
}