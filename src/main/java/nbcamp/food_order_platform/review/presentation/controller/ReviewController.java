package nbcamp.food_order_platform.review.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.review.application.dto.CreateReviewDto;
import nbcamp.food_order_platform.review.application.service.ReviewService;
import nbcamp.food_order_platform.review.presentation.dto.request.PatchReviewReqDto;
import nbcamp.food_order_platform.review.presentation.dto.request.PatchReviewStatusReqDto;
import nbcamp.food_order_platform.review.presentation.dto.request.PostReviewReqDto;
import nbcamp.food_order_platform.review.presentation.dto.response.GetReviewCustomerResDto;
import nbcamp.food_order_platform.review.presentation.dto.response.GetReviewManagerResDto;
import nbcamp.food_order_platform.review.presentation.dto.response.PatchReviewResDto;
import nbcamp.food_order_platform.review.presentation.dto.response.PostReviewResDto;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    /**
     * 1. 리뷰 작성
     */
    @PostMapping("/reviews")
    public ResponseEntity<PostReviewResDto> createReview(
            @Valid @RequestBody PostReviewReqDto reqDto
            // @AuthenticationPrincipal User currentUser // 나중에 시큐리티 붙으면 쓸 것!
    ) {
        // 지금은 임시로 userId를 1L이라고 가정
        Long currentUserId = 1L;

        // 1. 받은 reqDto와 시스템이 아는 userId를 합쳐서
        // 2. Service 로 보낼 CreateReviewDto 생성
        CreateReviewDto serviceDto = CreateReviewDto.builder()
                .orderId(reqDto.getOrderId())
                //.storeId(reqDto.getOrderId().getStoreId())
                .userId(currentUserId)
                .rating(reqDto.getRating())
                .content(reqDto.getContent())
                .build();

        return ResponseEntity.ok(reviewService.createReview(serviceDto));
    }

    /**
     * 2-1. 리뷰 수정 (내용/별점)
     */
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<PatchReviewResDto> updateReview(
            @PathVariable UUID reviewId,
            //@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PatchReviewReqDto reqDto) {

        // 지금은 임시로 userId를 1L이라고 가정
        Long currentUserId = 1L;

        PatchReviewResDto response = reviewService.updateReview(reviewId, currentUserId, reqDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 2-2. 리뷰 상태 변경 (숨김/노출) - MASTER, MANAGER 전용
     */
    @PatchMapping("/admin/reviews/{reviewId}/status")
    public ResponseEntity<PatchReviewResDto> changeReviewStatus(
            @PathVariable UUID reviewId,
            // @AuthenticationPrincipal User currentUser
            @Valid @RequestBody PatchReviewStatusReqDto dto) {

        // 지금은 임시로 MANAGER 권한을 가진 유저 객체를 생성해서 넘김
        // 추후 인증구현시 변경
        User managerUser = User.builder()
                .userId(2L)
                .role(Role.MANAGER) // 권한 체크 통과를 위해 MANAGER로 설정
                .build();

        PatchReviewResDto response = reviewService.changeReviewStatus(reviewId, managerUser, dto);
        return ResponseEntity.ok(response);
    }
    /**
     * 3. 리뷰 삭제
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        // 지금은 임시로 userId를 1L이라고 가정
        Long currentUserId = 1L;

        reviewService.deleteReview(reviewId, currentUserId);

        // 성공적으로 삭제(수정)되었으나 응답 본문은 비어있음
        return ResponseEntity.noContent().build();
    }
    /**
     * 4-1. 가게별 리뷰 조회(CUSTOMER용 - VISIBLE만 노출)
     */
    @GetMapping("/reviews/stores/{storeId}")
    public ResponseEntity<List<GetReviewCustomerResDto>> getStoreReviewsForCustomer(
            @PathVariable UUID storeId) {
        return ResponseEntity.ok(reviewService.getReviewsByStoreForCustomer(storeId));
    }
    /**
     * 4-2. 가게별 리뷰 조회 (MANAGER용 - 전체 노출)
     */
    @GetMapping("/admin/reviews/stores/{storeId}")
    public ResponseEntity<List<GetReviewManagerResDto>> getStoreReviewsForManager(
            @PathVariable UUID storeId) {

        // 권한 체크를 위해 임시 MANAGER 객체 생성
        User managerUser = User.builder()
                .userId(2L)
                .role(Role.MANAGER)
                .build();

        return ResponseEntity.ok(reviewService.getReviewsByStoreForManager(storeId, managerUser));
    }
    /**
     * 5. 특정 유저가 작성한 리뷰 목록 조회
     */
    @GetMapping("/reviews/users/{userId}")
    public ResponseEntity<List<GetReviewCustomerResDto>> getUserReviews(
            @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

}
