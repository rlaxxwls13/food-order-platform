package nbcamp.food_order_platform.review.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReviewStatusDto {

    private UUID reviewId;
    private Long userId;
    private ReviewStatus status;

    public static UpdateReviewStatusDto of(UUID reviewId, Long userId,ReviewStatus status) {
        return new UpdateReviewStatusDto(reviewId,userId,status);
    }
}
