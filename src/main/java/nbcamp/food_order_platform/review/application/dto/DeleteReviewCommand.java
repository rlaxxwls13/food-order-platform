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
public class DeleteReviewCommand {

    private UUID reviewId;
    private Long userId;


    public static DeleteReviewCommand of(UUID reviewId, Long userId) {
        return new DeleteReviewCommand(reviewId, userId);
    }
}
