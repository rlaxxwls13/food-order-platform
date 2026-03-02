package nbcamp.food_order_platform.review.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchReviewReqDto { // 수정 요청

    @Min(1) @Max(5)
    private int rating;
    private String content;
}
