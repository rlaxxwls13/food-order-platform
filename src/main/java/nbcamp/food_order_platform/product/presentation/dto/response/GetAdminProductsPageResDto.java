package nbcamp.food_order_platform.product.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbcamp.food_order_platform.product.application.dto.result.GetAdminProductsPageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetAdminProductsPageResDto {
    private List<ProductAdminSummaryDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductAdminSummaryDto {
        private UUID productId;
        private UUID storeId;
        private String name;
        private int price;
        private int stockQuantity;
        private boolean isHidden;
        private boolean isDeleted;
        private LocalDateTime updatedAt;
    }

    public static GetAdminProductsPageResDto from(GetAdminProductsPageResult result) {
        return GetAdminProductsPageResDto.builder()
                .content(result.getContent().stream()
                        .map(product -> ProductAdminSummaryDto.builder()
                                .productId(product.getProductId())
                                .storeId(product.getStoreId())
                                .name(product.getName())
                                .price(product.getPrice())
                                .stockQuantity(product.getStockQuantity())
                                .isHidden(product.isHidden())
                                .isDeleted(product.isDeleted())
                                .updatedAt(product.getUpdatedAt())
                                .build())
                        .toList())
                .page(result.getPage())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.isHasNext())
                .build();
    }
}
