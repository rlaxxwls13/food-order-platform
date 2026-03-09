package nbcamp.food_order_platform.store.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import nbcamp.food_order_platform.store.application.dto.common.StoreResult;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class GetStoresAdminPageResult {
    private List<StoreResult> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public static GetStoresAdminPageResult from(Page<StoreResult> page) {
        return GetStoresAdminPageResult.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }
}
