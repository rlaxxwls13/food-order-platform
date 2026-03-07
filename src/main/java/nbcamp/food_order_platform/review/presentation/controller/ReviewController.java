package nbcamp.food_order_platform.review.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.review.application.dto.*;
import nbcamp.food_order_platform.review.application.service.ReviewService;
import nbcamp.food_order_platform.review.presentation.dto.request.PatchReviewReqDto;
import nbcamp.food_order_platform.review.presentation.dto.request.PatchReviewStatusReqDto;
import nbcamp.food_order_platform.review.presentation.dto.request.PostReviewReqDto;
import nbcamp.food_order_platform.review.application.dto.GetReviewCustomerResult;
import nbcamp.food_order_platform.review.application.dto.GetReviewManagerResult;
import nbcamp.food_order_platform.review.application.dto.UpdateReviewResult;
import nbcamp.food_order_platform.review.application.dto.CreateReviewResult;

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
    public ResponseEntity<CreateReviewResult> createReview(
            @Valid @RequestBody PostReviewReqDto reqDto
            // @AuthenticationPrincipal UserDetailsImpl userDetails // 나중에 시큐리티 붙으면 쓸 것!
    ) {
        // [임시 코드] 시큐리티 적용 전까지는 하드코딩된 ID 사용
        Long currentUserId = 1L;

        /*  시큐리티 적용 시 아래 주석 해제 예정
        Long currentUserId = userDetails.getUser().getId()
        */

        // 1. 받은 reqDto와 시스템이 아는 userId를 합쳐서
        // 2. Service 로 보낼 CreateReviewDto 생성
        CreateReviewCommand serviceDto = CreateReviewCommand.of(currentUserId,reqDto.getOrderId(),reqDto.getRating(),reqDto.getContent());
        return ResponseEntity.ok(reviewService.createReview(serviceDto));
    }

    /**
     * 2-1. 리뷰 수정 (내용/별점)
     */
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<UpdateReviewResult> updateReview(
            @PathVariable UUID reviewId,
            //@AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PatchReviewReqDto reqDto) {

        // [임시 코드] 시큐리티 적용 전까지는 하드코딩된 ID 사용
        Long currentUserId = 1L;

        /*  시큐리티 적용 시 아래 주석 해제 예정
        Long currentUserId = userDetails.getUser().getId()
        */

        // 1. 받은 reqDto와 시스템이 아는 userId를 합쳐서
        // 2. Service 로 보낼 UpdateReviewDto 생성
        UpdateReviewCommand serviceDto = UpdateReviewCommand.of(reviewId,currentUserId,reqDto.getRating(),reqDto.getContent());
        return ResponseEntity.ok(reviewService.updateReview(serviceDto));
    }

    /**
     * 2-2. 리뷰 상태 변경 (숨김/노출) - MASTER, MANAGER 전용
     */
    @PatchMapping("/admin/reviews/{reviewId}/status")
    public ResponseEntity<UpdateReviewResult> changeReviewStatus(
            @PathVariable UUID reviewId,
            //@AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PatchReviewStatusReqDto dto) {

        // [임시 코드] 시큐리티 적용 전까지는 하드코딩된 ID 사용
        Long managerUserId = null; // 이상태로 하면 NPE터짐!

        /*  시큐리티 적용 시 아래 주석 해제 예정
        Long managerUser = userDetails.getUser().getUserId();
        */
        UpdateReviewStatusCommand serviceDto = UpdateReviewStatusCommand.of(reviewId,managerUserId,dto.getStatus());
        return ResponseEntity.ok(reviewService.changeReviewStatus(serviceDto));
    }
    /**
     * 3. 리뷰 삭제
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId
            //,@AuthenticationPrincipal UserDetailsImpl userDetails
            ) {
        // [임시 코드] 시큐리티 적용 전까지는 하드코딩된 ID 사용
        Long currentUserId = 1L;

        /*  시큐리티 적용 시 아래 주석 해제 예정
        Long currentUserId = userDetails.getUser().getId();
        */
        DeleteReviewCommand serviceDto = DeleteReviewCommand.of(reviewId, currentUserId);
        reviewService.deleteReview(serviceDto);

        // 성공적으로 삭제(수정)되었으나 응답 본문은 비어있음
        return ResponseEntity.noContent().build();
    }
    /**
     * 4-1. 가게별 리뷰 조회(CUSTOMER용 - VISIBLE만 노출)
     */
    @GetMapping("/reviews/stores/{storeId}")
    public ResponseEntity<List<GetReviewCustomerResult>> getStoreReviewsForCustomer(
            @PathVariable UUID storeId) {
        return ResponseEntity.ok(reviewService.getReviewsByStoreForCustomer(storeId));
    }
    /**
     * 4-2. 가게별 리뷰 조회 (MANAGER용 - 전체 노출)
     */
    @GetMapping("/admin/reviews/stores/{storeId}")
    public ResponseEntity<List<GetReviewManagerResult>> getStoreReviewsForManager(
            @PathVariable UUID storeId
            //,@AuthenticationPrincipal UserDetailsImpl userDetails
            ) {

        // [임시 코드] 시큐리티 적용 전까지는 하드코딩된 ID 사용
        Long managerUserId = 1L; // 이상태로 하면 NPE터짐!

        /*  시큐리티 적용 시 아래 주석 해제 예정
        Long managerUserId = userDetails.getUser().userId;
        */
        GetReviewManagerQuery serviceDto = GetReviewManagerQuery.forManager(storeId,managerUserId);
        return ResponseEntity.ok(reviewService.getReviewsByStoreForManager(serviceDto));
    }
    /**
     * 5. 특정 유저가 작성한 리뷰 목록 조회
     */
    @GetMapping("/reviews/users/{userId}")
    public ResponseEntity<List<GetReviewCustomerResult>> getUserReviews(
            @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

}
