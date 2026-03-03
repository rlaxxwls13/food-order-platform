package nbcamp.food_order_platform.review.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewDto { // 리뷰 작성, service에서 사용

    private UUID orderId;
    private UUID storeId;
    private Long userId;
    private int rating;
    private String content;

}
