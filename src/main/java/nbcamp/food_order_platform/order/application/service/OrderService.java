package nbcamp.food_order_platform.order.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.dto.command.OrderCreateCommand;
import nbcamp.food_order_platform.order.application.dto.command.OrderRejectCommand;
import nbcamp.food_order_platform.order.application.dto.query.OrderSearchQuery;
import nbcamp.food_order_platform.order.application.dto.result.OrderAddressInfo;
import nbcamp.food_order_platform.order.application.dto.result.OrderItemInfo;
import nbcamp.food_order_platform.order.application.dto.result.OrderResult;
import nbcamp.food_order_platform.order.application.dto.result.OrderSummaryResult;
import nbcamp.food_order_platform.order.domain.entity.*;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.user.domain.entity.Role;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
    public OrderResult createOrder(OrderCreateCommand command, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        validateSameUser(authUser, command.userId());
        Long userId = command.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        Store store = storeRepository.findById(command.storeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        Address address = addressRepository.findById(command.addressId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "존재하지 않는 주소입니다."));
        OrderAddress snapshotAddress = new OrderAddress(
                address.getPlaceName(),
                address.getRoadName(),
                address.getDetailName());


        Order order = Order.create(user, store);
        order.setSnapshotAddress(snapshotAddress);

        List<OrderCreateCommand.OrderItemCommand> itemRequests = command.items();
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "주문 상품이 비어있습니다.");
        }

        List<UUID> productIds = itemRequests.stream()
                .map(OrderCreateCommand.OrderItemCommand::productId)
                .distinct()
                .toList();
        //map 으로 반환 O(N) -> O(1)
        Map<UUID, Product> productMap = productRepository.findAllById(productIds).stream()
                .filter(p -> store.getId().equals(p.getStoreId()))
                .filter(p -> !p.isHidden())
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (OrderCreateCommand.OrderItemCommand itemReq : itemRequests) {
            Product product = Optional.ofNullable(productMap.get(itemReq.productId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

            order.addOrderItemWithStockAdjustment(product, itemReq.quantity());
        }
        Order savedOrder = orderRepository.save(order);
        return toOrderResult(savedOrder);
    }

    // 내 주문 상세 조회 (고객)
    public OrderResult getOrderCustomer(UUID orderId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        Long userId = authUser.getUserId();

        Order order = findOrderById(orderId);
        validateOrderOwner(order, userId);
        return toOrderResult(order);
    }

    // 가게 주문 상세 조회 (사장)
    public OrderResult getOrderOwner(UUID orderId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        Order order = findOrderById(orderId);
        validateStorePermission(order.getStore().getId(), authUser.getUserId(), role);
        return toOrderResult(order);
    }

    // 전체 주문 상세 조회 (관리자)
    public OrderResult getOrderAdmin(UUID orderId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateAdminPermission(role);
        return toOrderResult(findOrderById(orderId));
    }

    // 내 주문 페이징 검색 (고객)
    public Page<OrderSummaryResult> searchOrdersCustomer(AuthUser authUser, OrderSearchQuery query,
            Pageable pageable) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        validateSameUser(authUser, query.userId());

        return orderRepository.searchCustomerOrders(
                        authUser.getUserId(),
                        query.status(),
                        query.start(),
                        query.end(),
                        pageable)
                .map(this::toOrderSummaryResult);
    }
                
    // 가게 주문 페이징 검색 (사장)
    public Page<OrderSummaryResult> searchOrdersOwner(OrderSearchQuery query, Pageable pageable, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        if (query.storeId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "storeId는 필수입니다.");
        }
        validateStorePermission(query.storeId(), authUser.getUserId(), role);
        return orderRepository.searchOwnerOrders(
                        query.storeId(),
                        query.status(),
                        query.start(),
                        query.end(),
                        pageable)
                .map(this::toOrderSummaryResult);
    }
    // 전체 주문 페이징 검색 (관리자)
    public Page<OrderSummaryResult> searchOrdersAdmin(OrderSearchQuery query, Pageable pageable,
            AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateAdminPermission(role);
        return orderRepository
                .searchAdminOrders(query.status(), query.start(), query.end(), pageable)
                .map(this::toOrderSummaryResult);
    }

    @Transactional
    public void cancelOrderByUser(UUID orderId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateCustomerPermission(role);
        Long userId = authUser.getUserId();

        Order order = findOrderById(orderId);
        validateOrderOwner(order, userId);
        order.cancelByUser();
        if (order.getPayment() != null) {
            order.getPayment().cancel();
        }
    }

    // 사장 주문 승인
    @Transactional
    public void acceptOrderByOwner(UUID orderId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        Order order = findOrderById(orderId);
        validateStorePermission(order.getStore().getId(), authUser.getUserId(), role);
        order.acceptByOwner();
    }

    // 사장 주문 거절 및 환불
    @Transactional
    public void rejectOrderByOwner(UUID orderId, OrderRejectCommand command, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateRejectReason(command);
        Order order = findOrderById(orderId);
        validateStorePermission(order.getStore().getId(), authUser.getUserId(), role);
        order.rejectByOwner();
    }

    // 사장 주문 완료 처리 (STORE_ACCEPTED -> COMPLETED)
    @Transactional
    public void completeOrderByOwner(UUID orderId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        if (role != Role.OWNER) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "사장만 주문 완료 처리할 수 있습니다.");
        }
        Order order = findOrderById(orderId);
        validateStorePermission(order.getStore().getId(), authUser.getUserId(), role);
        order.completeByOwner();
    }

    // 관리자 주문 강제 취소
    @Transactional
    public void cancelOrderByAdmin(UUID orderId, AuthUser authUser) {
        Role role = requireAuthUser(authUser);
        validateAdminPermission(role);
        Order order = findOrderById(orderId);
        order.cancelByAdmin();
    }
    /**
     * 주문 ID로 주문 엔티티 조회 (존재하지 않을 경우 NOT_EXISTED_ORDER 예외 발생)
     */
    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ORDER));
    }
    /**
     * 주문서의 소유자와 현재 요청자가 일치하는지 검증
     */
    private void validateOrderOwner(Order order, Long userId) {
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }
    /**
     * 인증 정보(AuthUser)에서 권한(Role)을 추출하고 유효성 검증
     */
    private Role requireAuthUser(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.AUTHORIZATION);
        }
        try {
            return Role.valueOf(authUser.getRole());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_ROLE);
        }
    }
    /**
     * 현재 사용자가 일반 고객(CUSTOMER) 권한인지 확인
     */
    private void validateCustomerPermission(Role role) {
        if (role != Role.CUSTOMER) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }
    /**
     * 요청 데이터상의 사용자 ID와 실제 로그인한 사용자 ID가 일치하는지 확인
     */
    private void validateSameUser(AuthUser authUser, Long commandUserId) {
        if (commandUserId == null || !commandUserId.equals(authUser.getUserId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }
    /**
     * 관리자(MANAGER, MASTER) 권한 보유 여부 확인
     */
    private void validateAdminPermission(Role role) {
        if (role != Role.MANAGER && role != Role.MASTER) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "관리자 권한이 없습니다.");
        }
    }
    /**
     * 주문 거절/취소 시 필수 입력 사유가 포함되어 있는지 검증
     */
    private void validateRejectReason(OrderRejectCommand command) {
        if (command == null || command.reason() == null || command.reason().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "거절/취소 사유를 입력해주세요.");
        }
    }
    /**
     * 특정 가게에 대한 접근 권한 확인 (관리자 프리패스 또는 가게 주인 확인)
     */
    public void validateStorePermission(UUID storeId, Long userId, Role role) { //가게 주인 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        if (role == Role.MANAGER || role == Role.MASTER)
            return;

        if (role == Role.OWNER && store.getOwnerId().equals(userId))
            return;

        throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
    }
    // --- DTO 매핑 ---
    private OrderResult toOrderResult(Order order) {
        return OrderResult.builder()
                .orderId(order.getOrderId())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getName())
                .status(order.getOrderStatus())
                .totalPrice(order.getTotalAmount())
                .items(order.getOrderItems().stream().map(this::toOrderItemInfo).collect(Collectors.toList()))
                .address(toOrderAddressInfo(order.getSnapshotAddress()))
                .createdAt(order.getCreatedAt())
                .userInfo(new OrderResult.UserInfo(order.getUser().getUserId(), order.getUser().getUsername()))
                .build();
    }

    private OrderItemInfo toOrderItemInfo(OrderItem item) {
        return OrderItemInfo.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .status(item.getOrderItemStatus())
                .build();
    }

    private OrderAddressInfo toOrderAddressInfo(OrderAddress address) {
        if (address == null) return null;
        return OrderAddressInfo.builder()
                .placeName(address.getPlaceName())
                .roadName(address.getRoadName())
                .detailName(address.getDetailName())
                .build();
    }

    private OrderSummaryResult toOrderSummaryResult(Order order) {
        String repName = order.getOrderItems().isEmpty() ? "" : order.getOrderItems().get(0).getProductName();
        if (order.getOrderItems().size() > 1) {
            repName += " 외 " + (order.getOrderItems().size() - 1) + "건";
        }
        return OrderSummaryResult.builder()
                .orderId(order.getOrderId())
                .storeName(order.getStore().getName())
                .representativeItemName(repName)
                .totalAmount(order.getTotalAmount())
                .status(order.getOrderStatus())
                .statusDescription(order.getOrderStatus().getDescription())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
