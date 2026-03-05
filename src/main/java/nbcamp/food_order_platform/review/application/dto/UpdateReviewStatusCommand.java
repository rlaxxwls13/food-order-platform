package nbcamp.food_order_platform.review.application.dto;

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
public class UpdateReviewStatusCommand {

    private UUID reviewId;
    private Long userId;
    private ReviewStatus status;

    public static UpdateReviewStatusCommand of(UUID reviewId, Long userId, ReviewStatus status) {
        return new UpdateReviewStatusCommand(reviewId,userId,status);
    }
}
