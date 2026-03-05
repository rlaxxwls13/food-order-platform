package nbcamp.food_order_platform.review.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.review.application.dto.*;
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
import nbcamp.food_order_platform.review.application.dto.GetReviewCustomerResult;
import nbcamp.food_order_platform.review.application.dto.GetReviewManagerResult;
import nbcamp.food_order_platform.review.application.dto.UpdateReviewResult;
import nbcamp.food_order_platform.review.application.dto.CreateReviewResult;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
        // 1. 받아온 userId로 User 객체 조회
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        // 2. 받아온 orderId로 Order 객체 조회하고
        // Order와 User Id로 검증 절차 1-5(주문 존재/본인 여부/주문 완 료상태/3일이내/중복 리뷰 확인)
        // 해서 통과된 order만 받아서 리뷰 작성 가능.
        Order order = validateOrder(dto.getOrderId(), dto.getUserId());

        Store store = storeRepository.findById(order.getStore())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));

        // 검증 통과시 리뷰 작성 가능
        Review review = Review.builder()
                .order(order)                   // 조회한 Order 객체 넣기
                .store(store)                   // 조회한 Store 객체 넣기, 추후 .store(order.getStore())
                .user(user)                      // 조회한 User 객체 넣기
                .nickname(user.getNickname())     // User에서 꺼냄
                .rating(dto.getRating())
                .content(dto.getContent())
                .build();

        Review saved = reviewRepository.save(review);

        return CreateReviewResult.from(saved);
    }

    // 2-1. 리뷰 수정 (CUSTOMER)
    public UpdateReviewResult updateReview(UpdateReviewCommand dto) {
        Review review = reviewRepository.findById(dto.getReviewId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 본인 리뷰인지 확인
        if (!review.getUser().getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");
        }

        // 1. 엔티티 수정 (JPA 더티 체킹 감지로 save 호출 안해도 됨)
        review.updateReview(dto.getRating(), dto.getContent());
        return UpdateReviewResult.from(review);
    }

    // 2-2. 리뷰 수정,상태 변경 (MASTER,MANAGER)
    public UpdateReviewResult changeReviewStatus(UpdateReviewStatusCommand dto) {
        Review review = reviewRepository.findById(dto.getReviewId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("매니저/마스터를 찾을 수 없습니다."));

        // 권한 체크: 매니저도 아니고 마스터도 아니라면 에러
        if (user.getRole() != Role.MANAGER && user.getRole() != Role.MASTER) {
            throw new IllegalArgumentException("매니저 또는 마스터 권한이 필요합니다.");
        }

        // 엔티티의 updateStatus 메서드
        review.updateStatus(dto.getStatus());
        return UpdateReviewResult.from(review);
    }

    // 3. 리뷰 삭제
    public void deleteReview(DeleteReviewCommand dto) {
        Review review = reviewRepository.findById(dto.getReviewId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // review.getUser().getUserId() 와 들어온 usesrId 비교해 본인 체크.
        if (!review.getUser().getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        review.softDelete(dto.getUserId());
    }

    // 4-1. 리뷰 조회 - 가게 리뷰(CUSTOMER)
    @Transactional(readOnly = true)
    public List<GetReviewCustomerResult> getReviewsByStoreForCustomer(UUID storeId) {
        // 해당 가게의 리뷰 중 VISIBLE 상태인 것만 가져와 Customer DTO로 반환
        return reviewRepository.findAllByStoreId(storeId).stream()
                .filter(r -> r.getStatus() == ReviewStatus.VISIBLE)
                .map(GetReviewCustomerResult::from)
                .collect(Collectors.toList());
    }

    // 4-2. 리뷰 조회 - 가게 리뷰(MASTER/MANAGER)
    @Transactional(readOnly = true)
    public List<GetReviewManagerResult> getReviewsByStoreForManager(GetReviewManagerQuery dto) {
        // 권한 체크
        // 1. 유저를 찾고, 없으면 에러를
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("매니저/마스터를 찾을 수 없습니다."));

        if (user.getRole() != Role.MANAGER && user.getRole() != Role.MASTER) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        // 해당 가게의 모든 리뷰를 가져와서 Manager DTO로 반환 (상태값 포함)
        return reviewRepository.findAllByStoreId(dto.getStoreId()).stream()
                .map(GetReviewManagerResult::from)
                .collect(Collectors.toList());
    }

    // 리뷰 조회 - 특정 유저가 작성한 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<GetReviewCustomerResult> getReviewsByUser(Long targetUserId) {

        // 해당 유저가 쓴 모든 리뷰 가져오기
        List<Review> reviews = reviewRepository.findAllByUser_UserId(targetUserId);

        // 다른 유저가 볼 수있는 것이라 VISIBLE 상태인 리뷰만 필터링해서 반환
        return reviews.stream()
                .filter(review -> review.getStatus() == ReviewStatus.VISIBLE)
                .map(GetReviewCustomerResult::from)
                .collect(Collectors.toList());
    }


//  리뷰 작성시 검증 로직 메서드
//    추후 통일된 에러로 변경 임시로 Illegal->Custom 작성
  private Order validateOrder(UUID orderId, Long userId) {
    // 1. 주문 존재 확인
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문없음"));//new CustomException(ErrorCode.NOT_EXISTED_ORDER));
    // 2. 본인 주문 확인
       if (!Objects.equals(order.getUser().getUserId(), userId)) {
          throw new IllegalArgumentException("본인 주문이 아님");
      }
    // 3. 주문 완료 상태 확인
    if (order.getOrderStatus() != OrderStatus.COMPLETED) {
        //throw new CustomException(ErrorCode.ORDER_NOT_COMPLETE);
        throw new IllegalArgumentException("주문이 완료 상태가 아님");
    }
    // 4. 3일 이내 확인
    if (order.getCreatedAt().plusDays(3).isBefore(LocalDateTime.now())) {
        //throw new CustomException(ErrorCode.VALIDATION_FAILED);
        throw new IllegalArgumentException("현재 시간이 주문 생성으로부터 3일 이내가 아님");
    }
    // 5. 중복 리뷰 확인
    if (reviewRepository.existsByOrderOrderId(orderId)) {
        //throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        throw new IllegalArgumentException("이미 해당 orderId로 리뷰가 존재함");
    }
      return order;
 }



}
