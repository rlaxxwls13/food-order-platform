package nbcamp.food_order_platform.order.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.presentation.dto.request.OrderCreateRequest;
import nbcamp.food_order_platform.order.presentation.dto.response.OrderResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // 주문 생성 요청이 정상 응답(JSON)으로 반환되는지 검증
    @Test
    @DisplayName("주문 생성 요청 성공: 주문 정보가 JSON으로 잘 반환되는지 확인한다")
    void createOrder_success() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        OrderCreateRequest.OrderItemRequest item = new OrderCreateRequest.OrderItemRequest(UUID.randomUUID(), 1L);
        OrderCreateRequest request = new OrderCreateRequest(storeId, "배달 요청사항", List.of(item), addressId);

        OrderResponse response = OrderResponse.builder()
                .orderId(UUID.randomUUID())
                .storeId(storeId)
                .storeName("가게이름")
                .orderStatus(OrderStatus.CREATED)
                .totalAmount(10000L)
                .createdAt(LocalDateTime.now())
                .build();

        given(orderService.createOrder(any(OrderCreateRequest.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("가게이름"))
                .andExpect(jsonPath("$.orderStatus").value("CREATED"));
    }

    // 주문 단건 조회 요청이 정상 응답(JSON)으로 반환되는지 검증
    @Test
    @DisplayName("주문 단건 조회 성공: 주문 상세 정보가 JSON으로 잘 반환되는지 확인한다")
    void getOrder_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .storeId(UUID.randomUUID())
                .storeName("가게이름")
                .orderStatus(OrderStatus.CREATED)
                .totalAmount(10000L)
                .build();

        given(orderService.getOrderCustomer(eq(orderId), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.storeName").value("가게이름"));
    }
}
