package nbcamp.food_order_platform.review.presentation.dto.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReviewReqDto { // 리뷰 작성 요청

    private UUID orderId;
    @Min(1) @Max(5)
    private int rating; // 1-5 정수
    private String content;

}
