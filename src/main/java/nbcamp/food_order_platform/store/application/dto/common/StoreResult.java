package nbcamp.food_order_platform.store.application.dto.common;

import lombok.*;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.entity.StoreCategory;
import nbcamp.food_order_platform.store.domain.entity.StoreRegion;

import java.util.List;

@Getter
@Builder
public class StoreResult {
    private Store store;
    private StoreRegion storeRegion;
    private List<StoreCategory> storeCategories;

    public static StoreResult from(Store store, StoreRegion storeRegion, List<StoreCategory> storeCategories) {
        return StoreResult.builder()
                .store(store)
                .storeRegion(storeRegion)
                .storeCategories(storeCategories)
                .build();
    }
}
