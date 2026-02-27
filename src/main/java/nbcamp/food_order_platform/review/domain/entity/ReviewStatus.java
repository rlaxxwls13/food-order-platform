package nbcamp.food_order_platform.review.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewStatus {
    VISIBLE("노출"),
    HIDDEN("숨김"),
    DELETED("삭제");

    private final String description;
}

