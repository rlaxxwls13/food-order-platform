package nbcamp.food_order_platform.review.domain.repository;

import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 주문 존재 여부 반환
    boolean existsByOrderOrderId(UUID orderId);

    // 특정 가게의 리뷰 상태로 조회 (CUSTOMER)
    Slice<Review> findAllByStoreIdAndStatus(UUID storeId, ReviewStatus status, Pageable pageable);
    // 특정 가게의 리뷰 전체 조회
    Slice<Review> findAllByStoreId(UUID storeId, Pageable pageable);

    // 특정 유저가 쓴 리뷰 상태로 조회(CUSTOMER)
    Slice<Review> findAllByUser_UserIdAndStatus(Long userId,ReviewStatus status,Pageable pageable);
    // 특정 유저가 작성한 리뷰 "전체" 조회 (MANAGER/MASTER)
    Slice<Review> findAllByUser_UserId(Long userId, Pageable pageable);


}