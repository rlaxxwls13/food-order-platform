package nbcamp.food_order_platform.review.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReviewResDto { // 리뷰 작성 응답

    private UUID reviewId;
    private UUID storeId;
    private String nickname;

    private int rating;
    private String content;
    private ReviewStatus status;
    private LocalDateTime createdAt;


}
