package nbcamp.food_order_platform.review.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchReviewStatusReqDto { // 상태 변경 요청

    private ReviewStatus status;  // VISIBLE, HIDDEN
}