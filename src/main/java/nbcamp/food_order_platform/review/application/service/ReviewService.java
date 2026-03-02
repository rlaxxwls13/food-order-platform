package nbcamp.food_order_platform.review.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.review.application.dto.CreateReviewDto;
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
import nbcamp.food_order_platform.review.presentation.dto.*;
import nbcamp.food_order_platform.user.domain.Role;
import nbcamp.food_order_platform.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    // TODO: 내일 머지 후 주석 해제
    // private final OrderRepository orderRepository;
    // TODO: 내일 Store 객체 머지 후 주석 해제, 리뷰 갯수와 총 별점 갯수 갱신 로직 추가 예정.
    // private final StoreRepository storeRepository;

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

        // 권한 체크: 매니저도 아니고 마스터도 아니라면 에러
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
    public void deleteReview(UUID reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // review.getUser().getUserId() 와 매개 변수 currentUserId 비교해 본인 체크.
        if (!review.getUser().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    // 리뷰 조회 - 가게 리뷰(CUSTOMER)
    @Transactional(readOnly = true)
    public List<GetReviewCustomerResDto> getReviewsByStoreForCustomer(UUID storeId) {
        // 해당 가게의 리뷰 중 VISIBLE 상태인 것만 가져와 Customer DTO로 반환
        return reviewRepository.findAllByStoreId(storeId).stream()
                .filter(r -> r.getStatus() == ReviewStatus.VISIBLE)
                .map(this::convertToCustomerDto)
                .collect(Collectors.toList());
    }

    // 리뷰 조회 - 가게 리뷰 (관리자용)
    @Transactional(readOnly = true)
    public List<GetReviewManagerResDto> getReviewsByStoreForManager(UUID storeId, User currentUser) {
        // 권한 체크
        if (currentUser.getRole() != Role.MANAGER && currentUser.getRole() != Role.MASTER) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        // 해당 가게의 모든 리뷰를 가져와서 Manager DTO로 반환 (상태값 포함)
        return reviewRepository.findAllByStoreId(storeId).stream()
                .map(this::convertToManagerDto)
                .collect(Collectors.toList());
    }

    // 리뷰 조회 - 특정 유저가 작성한 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<GetReviewCustomerResDto> getReviewsByUser(Long targetUserId) {

        // 해당 유저가 쓴 모든 리뷰 가져오기
        List<Review> reviews = reviewRepository.findAllByUser_UserId(targetUserId);

        // 다른 유저가 볼 수있는 것이라 VISIBLE 상태인 리뷰만 필터링해서 반환
        return reviews.stream()
                .filter(review -> review.getStatus() == ReviewStatus.VISIBLE)
                .map(review -> GetReviewCustomerResDto.builder()
                        .reviewId(review.getReviewId())
                        .nickname(review.getNickname())
                        .rating(review.getRating())
                        .content(review.getContent())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 반환 메서드 (CUSTOMER)
    private GetReviewCustomerResDto convertToCustomerDto(Review review) {
        return GetReviewCustomerResDto.builder()
                .reviewId(review.getReviewId())
                .nickname(review.getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
    // 반환 메서드 (MASTER,MANAGER)
    private GetReviewManagerResDto convertToManagerDto(Review review) {
        return GetReviewManagerResDto.builder()
                .reviewId(review.getReviewId())
                .nickname(review.getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .status(review.getStatus())
                .build();
    }

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
