package nbcamp.food_order_platform.review.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.user.domain.User;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewDto { // 리뷰 작성, service에서 사용

    private UUID orderId;
    private UUID storeId;
    private User user; // Long userId → User 객체로 변경
    private int rating;
    private String content;

}
