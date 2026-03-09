package nbcamp.food_order_platform.store.presentation.dto.common;

import lombok.*;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.entity.StoreCategory;
import nbcamp.food_order_platform.store.domain.entity.StoreRegion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreResDto {
    private UUID storeId;
    private Long ownerId;
    private String name;
    private AddressDto address;
    private List<CategoryDto> categories;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private UUID regionCode;
        private String regionName;
        private String addressDetail;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private UUID categoryId;
        private String categoryName;
    }

    public static StoreResDto from(Store store, StoreRegion storeRegion, List<StoreCategory> storeCategories) {
        AddressDto addressDto = new AddressDto(
                storeRegion.getRegionCode().getCode(),
                storeRegion.getRegionCode().getRegionName(),
                storeRegion.getDetail()
        );

        List<CategoryDto> categoryDtoList = storeCategories.stream()
                .map(c -> new CategoryDto(c.getCategory().getId(), c.getCategory().getName()))
                .toList();

        return new StoreResDto(
                store.getId(),
                store.getOwnerId(),
                store.getName(),
                addressDto,
                categoryDtoList,
                store.getCreatedAt(),
                store.getCreatedBy(),
                store.getUpdatedAt(),
                store.getUpdatedBy(),
                store.getDeletedAt(),
                store.getDeletedBy()
        );
    }
}
