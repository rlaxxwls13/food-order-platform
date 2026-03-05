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
public class UpdateReviewCommand {

    private UUID reviewId;
    private Long userId;
    private int rating;
    private String content;


    public static UpdateReviewCommand of(UUID reviewId, Long userId, int rating, String content) {
        return new UpdateReviewCommand(reviewId, userId, rating, content);
    }

}
