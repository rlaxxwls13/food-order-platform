package nbcamp.food_order_platform.payment.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.payment.application.service.PaymentService;
import nbcamp.food_order_platform.payment.domain.entity.PaymentMethod;
import nbcamp.food_order_platform.payment.domain.entity.PaymentStatus;
import nbcamp.food_order_platform.payment.presentation.dto.request.PaymentCreateRequest;
import nbcamp.food_order_platform.payment.presentation.dto.response.PaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // 결제 생성 요청이 정상 응답(JSON)으로 반환되는지 검증
    @Test
    @DisplayName("결제 생성 요청 성공: 결제 정보가 JSON으로 잘 반환되는지 확인한다")
    void initiatePayment_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        PaymentCreateRequest request = new PaymentCreateRequest(orderId, 10000L, PaymentMethod.CARD);

        PaymentResponse response = new PaymentResponse(
                UUID.randomUUID(),
                orderId,
                10000L,
                PaymentStatus.READY,
                LocalDateTime.now());

        given(paymentService.initiatePayment(any(PaymentCreateRequest.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("READY"))
                .andExpect(jsonPath("$.totalAmount").value(10000L));
    }

    // 결제 단건 조회 요청이 정상 응답(JSON)으로 반환되는지 검증
    @Test
    @DisplayName("결제 상세 조회 성공: 결제 상세 정보가 JSON으로 잘 반환되는지 확인한다")
    void getPayment_success() throws Exception {
        // given
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                10000L,
                PaymentStatus.COMPLETED,
                LocalDateTime.now());

        given(paymentService.getPaymentCustomer(any(UUID.class), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }
}
