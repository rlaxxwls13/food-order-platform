package nbcamp.food_order_platform.review.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteReviewDto {

    private UUID reviewId;
    private Long userId;


    public static DeleteReviewDto of(UUID reviewId, Long userId) {
        return new DeleteReviewDto(reviewId, userId);
    }
}
