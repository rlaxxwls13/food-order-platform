package nbcamp.food_order_platform.review.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbcamp.food_order_platform.review.domain.entity.Review;

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
    public static GetReviewCustomerResDto from(Review review) {
        return GetReviewCustomerResDto.builder()
                .reviewId(review.getReviewId())
                .nickname(review.getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
