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
public class PostReviewDto { // 리뷰 작성, service에서 사용

    private Long userId;
    private UUID orderId;
    private int rating;
    private String content;

    // 컨트롤러에서 변환할 때 사용할 정적 팩토리 메서드
    public static PostReviewDto of(Long userId, UUID orderId, int rating, String content) {
        return new PostReviewDto(userId, orderId, rating, content);
    }
}

