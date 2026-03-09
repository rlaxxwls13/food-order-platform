package nbcamp.food_order_platform.review.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.review.application.dto.*;
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // 1. 리뷰 작성
    @Transactional
    public CreateReviewResult createReview(CreateReviewCommand dto) {
        // 1. 받아온 userId로 User 객체 조회 (에러: 유저가 존재하지 않음)
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        // 2. 받아온 orderId로 Order 객체 조회하고
        // Order와 User Id로 검증 절차 1-5(주문 존재/본인 여부/주문 완 료상태/3일이내/중복 리뷰 확인)
        Order order = validateOrder(dto.getOrderId(), dto.getUserId());

        // 검증 통과시 리뷰 작성 가능
        Review review = Review.builder()
                .order(order)                   // 조회한 Order 객체 넣기
                .store(order.getStore())        // 조회한 Store 객체 넣기
                .user(user)                      // 조회한 User 객체 넣기
                .nickname(user.getNickname())     // User에서 꺼냄
                .rating(dto.getRating())
                .content(dto.getContent())
                .build();

        Review saved = reviewRepository.save(review);
        order.getStore().addNewRating(dto.getRating()); // 주문한 가게의 총 평점 수 증가

        return CreateReviewResult.from(saved);
    }

    // 2-1. 리뷰 수정 (CUSTOMER)
    public UpdateReviewResult updateReview(UpdateReviewCommand dto) {
        Review review = hasReview(dto.getReviewId());

        // 본인 리뷰인지 확인 (에러:권한 없음)
        validateReviewOwner(review, dto.getUserId());

        // 수정 기간 체크 (똑같이 주문후 3일이내)
        if (review.getCreatedAt().plusDays(3).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        // 스토어 통계 업데이트 (수정된 리뷰의 별점 계산)
        Store store = review.getStore();
        int oldRating = review.getRating(); // 수정 전에 준 별점 저장(기존 별점)
        store.removeRating(oldRating);       // 기존 별점을 통계에서 빼기
        store.addNewRating(dto.getRating()); // 새로 바뀐 별점을 통계에 더하기

        // 엔티티 수정 (JPA 더티 체킹 감지로 save 호출 안해도 됨)
        review.updateReview(dto.getRating(), dto.getContent());
        return UpdateReviewResult.from(review);
    }

    // 2-2. 리뷰 상태 변경 (MASTER,MANAGER)
    public UpdateReviewResult changeReviewStatus(UpdateReviewStatusCommand dto) {
        Review review = hasReview(dto.getReviewId());

        // 엔티티의 updateStatus 메서드
        review.updateStatus(dto.getStatus());
        return UpdateReviewResult.from(review);
    }

    // 3. 리뷰 삭제
    public void deleteReview(DeleteReviewCommand dto) {
        Review review = hasReview(dto.getReviewId());

        // review.getUser().getUserId() 와 들어온 usesrId 비교해 본인 체크.
        validateReviewOwner(review, dto.getUserId());

        // 스토어 통계 업데이트 (삭제되는 리뷰의 별점 빼기)
        Store store = review.getStore();
        store.removeRating(review.getRating());

        review.softDelete(dto.getUserId());
    }

    // 4-1. 가게 리뷰 조회(CUSTOMER)
    @Transactional(readOnly = true)
    public Slice<GetReviewCustomerResult> getReviewsByStoreForCustomer(UUID storeId, Pageable pageable) {
        // DB에서 페이징된 엔티티 조회 (VISIBLE 상태만)
        Slice<Review> reviews = reviewRepository.findAllByStoreIdAndStatus(storeId, ReviewStatus.VISIBLE, pageable);
        // .map()을 사용해 엔티티를 DTO로 변환
        return reviews.map(GetReviewCustomerResult::from);
    }

    // 4-2. 가게 리뷰 조회(MASTER/MANAGER)
    @Transactional(readOnly = true)
    public Slice<GetReviewManagerResult> getReviewsByStoreForManager(GetReviewManagerQuery dto) {

        // 모든 리뷰 가져오기(VISIBLE,HIDDEN까지)
        return reviewRepository.findAllByStoreId(dto.getStoreId(), dto.getPageable())
                .map(GetReviewManagerResult::from);
    }

    // 5-1. 유저 리뷰 조회 (CUSTOMER)
    @Transactional(readOnly = true)
    public Slice<GetReviewCustomerResult> getReviewsByUserForCustomer(Long targetUserId,Pageable pageable) {
        // 해당 유저가 쓴 모든 리뷰 가져오기(VISIBLE)
        // .map()을 사용해 엔티티를 DTO로 변환
        return reviewRepository.findAllByUser_UserIdAndStatus(targetUserId,ReviewStatus.VISIBLE,pageable)
                .map(GetReviewCustomerResult::from);
    }
    // 5-2. 유저 리뷰 조회(MASTER/MANAGER)
    @Transactional(readOnly = true)
    public Slice<GetReviewManagerResult> getReviewsByUserForManager(GetReviewManagerQuery dto) {

        // 해당 유저가 쓴 모든 리뷰 가져오기(VISIBLE,HIDDEN까지)
        // .map()을 사용해 엔티티를 DTO로 변환
        return reviewRepository.findAllByUser_UserId(dto.getTargetUserId(),dto.getPageable())
                .map(GetReviewManagerResult::from);
    }

    // 리뷰 작성시 검증 로직 메서드
    private Order validateOrder(UUID orderId, Long userId) {
        // 1. 주문 존재 확인
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ORDER));
        // 2. 가게 존재 확인
        storeRepository.findById(order.getStore().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        // 3. 본인 주문 확인
        if (!Objects.equals(order.getUser().getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        // 4. 주문 완료 상태 확인
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        // 5. 3일 이내 확인
        if (order.getCreatedAt().plusDays(3).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        // 6. 중복 리뷰 확인
        if (reviewRepository.existsByOrderOrderId(orderId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
      return order;
 }

    // 리뷰 존재 검증 메서드
    private Review hasReview(UUID dto) {
        Review review = reviewRepository.findById(dto)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_REVIEW));
        return review;
    }

    // 리뷰 소유자 검증 메서드
    private void validateReviewOwner(Review review, Long userId) {
        if (!review.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
    }

}
