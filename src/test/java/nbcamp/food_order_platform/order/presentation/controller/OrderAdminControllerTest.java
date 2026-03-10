package nbcamp.food_order_platform.order.presentation.controller;

import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.order.application.dto.result.OrderSummaryResult;
import nbcamp.food_order_platform.order.application.service.OrderService;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderAdminController.class
)
@AutoConfigureMockMvc
class OrderAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // 관리자 주문 검색 요청이 정상적으로 페이지 응답되는지 검증
    @Test
    @DisplayName("관리자 주문 검색 성공: 전체 주문 내역이 페이지로 반환된다")
    void searchOrders_success() throws Exception {
        OrderSummaryResult summary = OrderSummaryResult.builder()
                .orderId(UUID.randomUUID())
                .storeName("관리자가게")
                .representativeItemName("대표상품")
                .totalAmount(15000L)
                .status(OrderStatus.CREATED)
                .statusDescription("생성 완료")
                .createdAt(LocalDateTime.now())
                .build();

        given(orderService.searchOrdersAdmin(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/admin/orders")
                        .with(user(new AuthUser(99L, "admin", "MANAGER")))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].storeName").value("관리자가게"))
                .andExpect(jsonPath("$.content[0].orderStatus").value("CREATED"));
    }

    // 관리자 주문 취소 요청이 OK 응답으로 처리되는지 검증
    @Test
    @DisplayName("관리자 주문 취소 성공: 취소 요청 시 OK를 반환한다")
    void cancelOrder_success() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/orders/{orderId}/cancel", orderId)
                        .with(user(new AuthUser(99L, "admin", "MANAGER")))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService).cancelOrderByAdmin(eq(orderId), any());
    }
}
