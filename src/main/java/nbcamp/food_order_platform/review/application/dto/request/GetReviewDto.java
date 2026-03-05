package nbcamp.food_order_platform.review.application.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetReviewDto {
    private final UUID storeId;
    private final Long userId; // 매니저일 때만 들어옴.

    // 가게조회 매니저용 (가게 ID + 유저 ID 둘 다 보낼 때)
    public static GetReviewDto forManager(UUID storeId, Long userId) {
        return new GetReviewDto(storeId,userId);
    }


}
