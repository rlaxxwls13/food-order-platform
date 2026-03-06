package nbcamp.food_order_platform.review.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetReviewManagerResDto {
    private UUID reviewId;
    private String nickname;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private ReviewStatus status;

    public static GetReviewManagerResDto from(Review review) {
        return GetReviewManagerResDto.builder()
                .reviewId(review.getReviewId())
                .nickname(review.getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .status(review.getStatus())
                .build();
    }
}
