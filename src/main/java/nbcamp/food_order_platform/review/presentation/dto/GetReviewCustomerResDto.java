package nbcamp.food_order_platform.review.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetReviewCustomerResDto {
    private UUID reviewId;
    private String nickname;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    // status 없음
}
