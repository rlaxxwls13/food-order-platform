package nbcamp.food_order_platform.review.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetReviewManagerQuery {
    private final UUID storeId;
    private final Long targetUserId; // 특정 유저 조회 시 사용 (Long)
    private final Long managerId;    // 권한 확인용 매니저 ID
    private final Pageable pageable;

    // 가게조회 매니저용 (가게 ID + 유저 ID 둘 다 보낼 때)
    public static GetReviewManagerQuery storeForManager(UUID storeId, Long userId, Pageable pageable) {
        return new GetReviewManagerQuery(storeId,null,userId,pageable);
    }
    // 유저조회 매니저용 (가게 ID + 유저 ID 둘 다 보낼 때)
    public static GetReviewManagerQuery userForManager(Long targetUserId, Long managerId, Pageable pageable) {
        return new GetReviewManagerQuery(null,targetUserId,managerId,pageable);
    }


}
