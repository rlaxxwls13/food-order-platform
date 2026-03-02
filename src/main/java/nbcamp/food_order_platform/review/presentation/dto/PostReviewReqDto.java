package nbcamp.food_order_platform.review.presentation.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostReviewReqDto { // 리뷰 작성 요청

    private UUID orderId;
    private int rating; // 1-5 정수
    private String content;

}
