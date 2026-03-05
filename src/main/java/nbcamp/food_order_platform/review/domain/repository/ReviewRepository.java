package nbcamp.food_order_platform.review.domain.repository;

import nbcamp.food_order_platform.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 주문 존재 여부 반환
    boolean existsByOrderOrderId(UUID orderId);

    // 특정 가게의 리뷰만 조회
    List<Review> findAllByStoreId(UUID storeId);

    // 특정 유저가 쓴 리뷰만 조회
    List<Review> findAllByUser_UserId(Long userId);
}