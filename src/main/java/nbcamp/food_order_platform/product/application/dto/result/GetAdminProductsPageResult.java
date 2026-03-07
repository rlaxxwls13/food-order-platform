package nbcamp.food_order_platform.product.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetAdminProductsPageResult {
    private List<ProductAdminSummary> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductAdminSummary {
        private UUID productId;
        private UUID storeId;
        private String name;
        private int price;
        private int stockQuantity;
        private boolean isHidden;
        private boolean isDeleted;
        private LocalDateTime updatedAt;
    }

    public static GetAdminProductsPageResult from(Page<ProductAdminSummary> page) {
        return GetAdminProductsPageResult.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }
}
