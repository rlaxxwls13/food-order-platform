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
        controllers = PaymentOwnerController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class PaymentOwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // 사장 결제 검색 요청이 정상적으로 페이지 응답되는지 검증
    @Test
    @DisplayName("사장 결제 검색 성공: 가게 결제 내역이 페이지로 반환된다")
    void searchPayments_success() throws Exception {
        UUID storeId = UUID.randomUUID();

        PaymentSummaryResponse summary = new PaymentSummaryResponse(
                UUID.randomUUID(),
                10000L,
                LocalDateTime.now(),
                PaymentStatus.READY,
                PaymentMethod.CARD
        );

        given(paymentService.searchPaymentsOwner(eq(storeId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/owner/payments")
                        .param("storeId", storeId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentStatus").value("READY"))
                .andExpect(jsonPath("$.content[0].totalAmount").value(10000L));
    }

    // 사장 결제 상세 조회 요청이 정상 응답(JSON)으로 반환되는지 검증
    @Test
    @DisplayName("사장 결제 상세 조회 성공: 결제 상세 정보가 JSON으로 반환된다")
    void getPayment_success() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                12000L,
                PaymentStatus.COMPLETED,
                LocalDateTime.now()
        );

        given(paymentService.getPaymentOwner(eq(paymentId), eq(storeId), any())).willReturn(response);

        mockMvc.perform(get("/api/v1/owner/payments/{paymentId}", paymentId)
                        .param("storeId", storeId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }
}
