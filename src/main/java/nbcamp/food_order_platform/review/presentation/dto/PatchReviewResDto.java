package nbcamp.food_order_platform.review.presentation.dto;

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
public class PatchReviewResDto {

    private UUID reviewId;
    private UUID storeId;
    private String nickname;
    private int rating;
    private String content;
    private ReviewStatus status;
    private LocalDateTime updatedAt;

    public static PatchReviewResDto from(Review review) {
        return PatchReviewResDto.builder()
                .reviewId(review.getReviewId())
                .storeId(review.getStoreId())
                .nickname(review.getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}

