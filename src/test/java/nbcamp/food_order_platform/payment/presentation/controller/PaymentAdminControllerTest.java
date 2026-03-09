package nbcamp.food_order_platform.payment.presentation.controller;

import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.payment.application.service.PaymentService;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentResponse;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentAdminController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class PaymentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // 관리자 결제 검색 요청이 정상적으로 페이지 응답되는지 검증
    @Test
    @DisplayName("관리자 결제 검색 성공: 전체 결제 내역이 페이지로 반환된다")
    void searchPayments_success() throws Exception {
        PaymentSummaryResponse summary = new PaymentSummaryResponse(
                UUID.randomUUID(),
                20000L,
                LocalDateTime.now(),
                PaymentStatus.COMPLETED,
                PaymentMethod.CARD
        );

        given(paymentService.searchPaymentsAdmin(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/admin/payments")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].totalAmount").value(20000L));
    }

    // 관리자 결제 상세 조회 요청이 정상 응답(JSON)으로 반환되는지 검증
    @Test
    @DisplayName("관리자 결제 상세 조회 성공: 결제 상세 정보가 JSON으로 반환된다")
    void getPayment_success() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                20000L,
                PaymentStatus.READY,
                LocalDateTime.now()
        );

        given(paymentService.getPaymentAdmin(eq(paymentId), any())).willReturn(response);

        mockMvc.perform(get("/api/v1/admin/payments/{paymentId}", paymentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.paymentStatus").value("READY"));
    }
}
