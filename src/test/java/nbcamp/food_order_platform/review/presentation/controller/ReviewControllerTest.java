package nbcamp.food_order_platform.review.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nbcamp.food_order_platform.review.application.dto.*;
import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.review.application.dto.CreateReviewDto;
import nbcamp.food_order_platform.review.application.service.ReviewService;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;
import nbcamp.food_order_platform.review.presentation.dto.request.PatchReviewReqDto;
import nbcamp.food_order_platform.review.presentation.dto.request.PatchReviewStatusReqDto;
import nbcamp.food_order_platform.review.presentation.dto.request.PostReviewReqDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 시큐리티 필터 때문에 401 에러가 나는 걸 막기 위해 임시로 제외
@WebMvcTest(
        controllers = ReviewController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc; // 가짜 요청(GET, POST 등)을 쏴주는 마법의 객체

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로, JSON을 객체로 바꿔주는 녀석

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("1. 리뷰 작성 성공 테스트")
    void createReview_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        PostReviewReqDto reqDto = PostReviewReqDto.builder()
                .orderId(orderId)
                .rating(5)
                .content("너무 맛있어요!")
                .build();

        CreateReviewResult resDto = CreateReviewResult.builder()
                .reviewId(UUID.randomUUID())
                .nickname("수빈")
                .rating(5)
                .content("너무 맛있어요!")
                .build();

        given(reviewService.createReview(any(CreateReviewCommand.class))).willReturn(resDto);

        // when & then
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("수빈"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @DisplayName("2-1. 리뷰 수정 성공 테스트")
    void updateReview_success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        PatchReviewReqDto reqDto = new PatchReviewReqDto(4, "내용 수정");

        UpdateReviewResult resDto = UpdateReviewResult.builder()
                .reviewId(reviewId)
                .rating(4)
                .content("내용 수정")
                .build();

        given(reviewService.updateReview(any(UpdateReviewCommand.class))).willReturn(resDto);

        // when & then
        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.content").value("내용 수정"));
    }

    @Test
    @DisplayName("2-2. 리뷰 상태 변경 (관리자용) 성공 테스트")
    void changeReviewStatus_success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        PatchReviewStatusReqDto reqDto = new PatchReviewStatusReqDto(ReviewStatus.HIDDEN);

        UpdateReviewResult resDto = UpdateReviewResult.builder()
                .reviewId(reviewId)
                .status(ReviewStatus.HIDDEN)
                .build();

        given(reviewService.changeReviewStatus(any(UpdateReviewStatusCommand.class))).willReturn(resDto);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/reviews/{reviewId}/status", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));
    }

    @Test
    @DisplayName("3. 리뷰 삭제 성공 테스트")
    void deleteReview_success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();

        // void 반환형인 service 메서드 모킹
        doNothing().when(reviewService).deleteReview(any(DeleteReviewCommand.class));

        // when & then
        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId))
                .andDo(print())
                .andExpect(status().isNoContent()); // 204 검증
    }

    @Test
    @DisplayName("4-1. 가게별 리뷰 조회 (CUSTOMER용) 성공 테스트 - 리스트 반환")
    void getStoreReviewsForCustomer_success() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();

        GetReviewCustomerResult review1 = GetReviewCustomerResult.builder()
                .reviewId(UUID.randomUUID())
                .nickname("배부른강아지")
                .rating(5)
                .content("진짜 인생 맛집이에요!")
                .build();

        GetReviewCustomerResult review2 = GetReviewCustomerResult.builder()
                .reviewId(UUID.randomUUID())
                .nickname("배고픈고양이")
                .rating(4)
                .content("양도 많고 맛있어요.")
                .build();

        given(reviewService.getReviewsByStoreForCustomer(storeId)).willReturn(List.of(review1, review2));

        // when & then
        mockMvc.perform(get("/api/v1/reviews/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nickname").value("배부른강아지"))
                .andExpect(jsonPath("$[1].rating").value(4));
    }

    @Test
    @DisplayName("4-2. 가게별 리뷰 조회 (MANAGER용) 성공 테스트")
    void getStoreReviewsForManager_success() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();

        GetReviewManagerResult review1 = GetReviewManagerResult.builder()
                .reviewId(UUID.randomUUID())
                .nickname("최고영이")
                .status(ReviewStatus.VISIBLE)
                .build();

        GetReviewManagerResult review2 = GetReviewManagerResult.builder()
                .reviewId(UUID.randomUUID())
                .nickname("악플러")
                .status(ReviewStatus.HIDDEN) // 매니저니까 숨김도 보임
                .build();

        given(reviewService.getReviewsByStoreForManager(any(GetReviewManagerQuery.class)))
                .willReturn(List.of(review1, review2));

        // when & then
        mockMvc.perform(get("/api/v1/admin/reviews/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].status").value("HIDDEN"));
    }
}