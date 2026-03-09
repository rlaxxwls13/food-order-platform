package nbcamp.food_order_platform.store.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresPageResult;
import nbcamp.food_order_platform.store.presentation.dto.common.StoreResDto;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetStoresPageResDto {
    private List<StoreResDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public static GetStoresPageResDto from(GetStoresPageResult result) {
        return GetStoresPageResDto.builder()
                .content(result.getContent().stream()
                        .map(storeResult -> StoreResDto.from(
                                storeResult.getStore(),
                                storeResult.getStoreRegion(),
                                storeResult.getStoreCategories()
                        ))
                        .toList())
                .page(result.getPage())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.isHasNext())
                .build();
    }
}
