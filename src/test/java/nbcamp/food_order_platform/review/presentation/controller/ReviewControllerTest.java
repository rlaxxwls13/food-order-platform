package nbcamp.food_order_platform.review.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.review.application.dto.CreateReviewDto;
import nbcamp.food_order_platform.review.application.service.ReviewService;
import nbcamp.food_order_platform.review.presentation.dto.request.PostReviewReqDto;
import nbcamp.food_order_platform.review.presentation.dto.response.GetReviewCustomerResDto;
import nbcamp.food_order_platform.review.presentation.dto.response.PostReviewResDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 1. 시큐리티 필터 때문에 401 에러가 나는 걸 막기 위해 임시로 시큐리티를 제외합니다!
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
    private ReviewService reviewService; // Service는 가짜(Mock)로 띄웁니다.

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("리뷰 작성 성공 테스트")
    void createReview_success() throws Exception {
        // given: 이런 데이터가 주어질 때
        UUID orderId = UUID.randomUUID();

        // Builder 패턴으로 생성
        PostReviewReqDto reqDto = PostReviewReqDto.builder()
                .orderId(orderId)
                .rating(5)
                .content("너무 맛있어요!")
                .build();

        PostReviewResDto resDto = PostReviewResDto.builder()
                .reviewId(UUID.randomUUID())
                .nickname("수빈")
                .rating(5)
                .content("너무 맛있어요!")
                .build();

        // 어떤 CreateReviewDto 객체가 들어오든지 상관없이 resDto를 반환.
        given(reviewService.createReview(any(CreateReviewDto.class))).willReturn(resDto);

        // when & then: 이 URL로 POST 요청을 보냈을 때 예상되는 결과
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto))) // DTO를 JSON 문자열로 변환해서 바디에 넣음
                .andDo(print()) // 콘솔에 요청/응답 과정을 출력
                .andExpect(status().isOk()) // 200 OK가 나오기
                .andExpect(jsonPath("$.nickname").value("수빈")) // JSON 응답에 nickname이 수빈
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @DisplayName("가게별 리뷰 조회 (CUSTOMER용) 성공 테스트")
    void getStoreReviewsForCustomer_success1() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/v1/reviews/stores/{storeId}", storeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게별 리뷰 조회 (CUSTOMER용) 성공 테스트 - 리스트 반환")
    void getStoreReviewsForCustomer_success2() throws Exception {
        // 1. given: 테스트 데이터 준비
        UUID storeId = UUID.randomUUID();

        // 여러 개의 리뷰 DTO를 빌더로 생성해서 리스트에 담기
        GetReviewCustomerResDto review1 = GetReviewCustomerResDto.builder()
                .reviewId(UUID.randomUUID())
                .nickname("맛점수빈")
                .rating(5)
                .content("진짜 인생 맛집이에요!")
                .build();

        GetReviewCustomerResDto review2 = GetReviewCustomerResDto.builder()
                .reviewId(UUID.randomUUID())
                .nickname("배고픈고양이")
                .rating(4)
                .content("양도 많고 맛있어요.")
                .build();

        List<GetReviewCustomerResDto> reviewList = List.of(review1, review2);

        // 리뷰서비스 실행되면 가져오는 결과 넣기
        given(reviewService.getReviewsByStoreForCustomer(storeId)).willReturn(reviewList);

        // 2. when & then: 요청 및 검증
        mockMvc.perform(get("/api/v1/reviews/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // 리스트 검증
                .andExpect(jsonPath("$.length()").value(2)) // 리스트 크기가 2개인지 확인
                .andExpect(jsonPath("$[0].nickname").value("맛점수빈")) // 첫 번째 리뷰 작성자 확인
                .andExpect(jsonPath("$[1].rating").value(4))     // 두 번째 리뷰 별점 확인
                .andExpect(jsonPath("$[0].content").value("진짜 인생 맛집이에요!"));
    }
}