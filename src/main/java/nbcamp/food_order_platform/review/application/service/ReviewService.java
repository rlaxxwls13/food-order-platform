package nbcamp.food_order_platform.review.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.review.application.dto.CreateReviewDto;
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
import nbcamp.food_order_platform.review.presentation.dto.PostReviewReqDto;
import nbcamp.food_order_platform.review.presentation.dto.PostReviewResDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    // private final OrderRepository orderRepository; // 머지 후 추가

    // 리뷰 작성
    public PostReviewResDto createReview(CreateReviewDto dto) {

        // 나머지 머지 후
        // 검증 로직 주석 해제
        // validateOrder(dto.getOrderId(), dto.getUser().getId());

        Review review = Review.builder()
                .orderId(dto.getOrderId())
                .storeId(dto.getStoreId())
                .user(dto.getUser())
                .nickname(dto.getUser().getNickname())
                .rating(dto.getRating())
                .content(dto.getContent())
                .build();

        Review saved = reviewRepository.save(review);

        return PostReviewResDto.builder()
                .reviewId(saved.getReviewId())
                .storeId(saved.getStoreId())
                .nickname(saved.getNickname())
                .rating(saved.getRating())
                .content(saved.getContent())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // 리뷰 수정
    public void updateReview() {}

    // 리뷰 삭제
    public void deleteReview() {}

    // 리뷰 조회
    public void getReviews() {}

//  private void validateOrder(UUID orderId, Long userId) {
//    // 1. 주문 존재 확인
//    Order order = orderRepository.findById(orderId)
//            .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTED_ORDER));
//    // 2. 본인 주문 확인
//    if (!order.getUserId().equals(userId)) {
//        throw new CustomException(ErrorCode.NO_PERMISSION);
//    }
//    // 3. 주문 완료 상태 확인
//    if (order.getStatus() != OrderStatus.COMPLETE) {
//        throw new CustomException(ErrorCode.ORDER_NOT_COMPLETE);
//    }
//    // 4. 3일 이내 확인
//    if (order.getCreatedAt().plusDays(3).isBefore(LocalDateTime.now())) {
//        throw new CustomException(ErrorCode.VALIDATION_FAILED);
//    }
//    // 5. 중복 리뷰 확인
//    if (reviewRepository.existsByOrderId(orderId)) {
//        throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
//    }
// }



}
