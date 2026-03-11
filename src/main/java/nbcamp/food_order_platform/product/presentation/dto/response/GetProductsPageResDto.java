package nbcamp.food_order_platform.product.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbcamp.food_order_platform.product.application.dto.result.GetProductsPageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetProductsPageResDto {
    private List<ProductPageSummaryDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductPageSummaryDto {
        private UUID productId;
        private UUID storeId;
        private String name;
        private int price;
        private int stockQuantity;
        private boolean isHidden;
        private LocalDateTime createdAt;
    }

    public static GetProductsPageResDto from(GetProductsPageResult result) {
        return GetProductsPageResDto.builder()
                .content(result.getContent().stream()
                        .map(product -> ProductPageSummaryDto.builder()
                                .productId(product.getProductId())
                                .storeId(product.getStoreId())
                                .name(product.getName())
                                .price(product.getPrice())
                                .stockQuantity(product.getStockQuantity())
                                .isHidden(product.isHidden())
                                .createdAt(product.getCreatedAt())
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
