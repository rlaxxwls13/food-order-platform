package nbcamp.food_order_platform.review.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.review.application.dto.CreateReviewDto;
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
import nbcamp.food_order_platform.review.presentation.dto.*;
import nbcamp.food_order_platform.user.domain.Role;
import nbcamp.food_order_platform.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    // TODO: 내일 머지 후 주석 해제
    // private final OrderRepository orderRepository;

    // 리뷰 작성
    public PostReviewResDto createReview(CreateReviewDto dto) {

        // TODO: 내일 Order 객체 머지 후 주석 해제 및 검증 로직 연결
        // validateOrder(dto.getOrderId(), dto.getUser().getId());

        Review review = Review.builder()
                .orderId(dto.getOrderId()) // 머지 전까진 컨트롤러에서 임시 UUID를 넘겨줄 예정
                .storeId(dto.getStoreId()) // 머지 전까진 컨트롤러에서 임시 UUID를 넘겨줄 예정
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

    // 리뷰 수정 (CUSTOMER)
    public PatchReviewResDto updateReview(UUID reviewId, Long userId, PatchReviewReqDto dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 본인 리뷰인지 확인
        if (!review.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");
        }

        // 엔티티 수정 (JPA 더티 체킹 감지로 save 호출 안해도 됨)
        review.updateReview(dto.getRating(), dto.getContent());
        return convertToPatchResDto(review);
    }

    // 리뷰 수정 (상태 변경) (MASTER,MANAGER)
    public PatchReviewResDto updateReviewStatus(UUID reviewId, User user, PatchReviewStatusReqDto dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        Role userRole = user.getRole();

        // 권한 체크 로직 (필요에 따라 추가)
        // 1. 권한 체크: 매니저도 아니고 마스터도 아니라면 에러!
        if (user.getRole() != Role.MANAGER && user.getRole() != Role.MASTER) {
            throw new IllegalArgumentException("매니저 또는 마스터 권한이 필요합니다.");
        }

        // 엔티티의 updateStatus 메서드
        review.updateStatus(dto.getStatus());
        // 변경된 결과를 PatchReviewResDto에 담아서 반환 (method)
        return convertToPatchResDto(review);
    }

    private static PatchReviewResDto convertToPatchResDto(Review review) {
        return PatchReviewResDto.builder()
                .reviewId(review.getReviewId())
                .storeId(review.getStoreId())
                .nickname(review.getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus()) // 변경된 상태값
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    // 리뷰 삭제
    public void deleteReview() {}

    // 리뷰 조회
    public void getReviews() {}


//  리뷰 작성시 검증 로직 메서드
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
