package nbcamp.food_order_platform.product.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.product.application.dto.command.CreateProductCommand;
import nbcamp.food_order_platform.product.application.dto.command.UpdateProductCommand;
import nbcamp.food_order_platform.product.application.dto.query.GetAdminProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.query.GetProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.result.*;
import nbcamp.food_order_platform.product.application.service.ProductService;
import nbcamp.food_order_platform.product.presentation.dto.request.PatchProductHiddenReqDto;
import nbcamp.food_order_platform.product.presentation.dto.request.PatchProductReqDto;
import nbcamp.food_order_platform.product.presentation.dto.request.PostProductReqDto;
import nbcamp.food_order_platform.product.presentation.dto.response.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<PostProductResDto> createProduct(@RequestBody PostProductReqDto requestDto) {

        CreateProductCommand productDto = new CreateProductCommand(
                requestDto.getStoreId(),
                requestDto.getName(),
                requestDto.getStockQuantity(),
                requestDto.getPrice(),
                requestDto.getDescription(),
                requestDto.isUseAi()
        );

        CreateProductResult result = productService.createProduct(productDto);
        return ResponseEntity.ok(new PostProductResDto(result));
    }

    @PatchMapping("/products/{productId}")
    public ResponseEntity<PatchProductResDto> updateProduct(@PathVariable UUID productId, @RequestBody PatchProductReqDto requestDto) {

        UpdateProductCommand productDto = new UpdateProductCommand(
                productId,
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getAddStockQuantity(),
                requestDto.getSetStockQuantity(),
                requestDto.getPrice(),
                requestDto.getUseAi()
        );

        UpdateProductResult result = productService.updateProduct(productDto);
        return ResponseEntity.ok(new PatchProductResDto(result));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<GetProductResDto> getProduct(@PathVariable UUID productId) {
        GetProductResult result = productService.getProduct(productId);
        return ResponseEntity.ok(new GetProductResDto(result));
    }

    @GetMapping("/products")
    public ResponseEntity<GetProductsPageResDto> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));

        GetProductsPageQuery query = GetProductsPageQuery.from(storeId, keyword);
        GetProductsPageResult result = productService.getProducts(query, pageable);

        return ResponseEntity.ok(GetProductsPageResDto.from(result));
    }

    @PatchMapping("/products/{productId}/hidden")
    public ResponseEntity<PatchProductHiddenResDto> updateProductHidden(
            @AuthenticationPrincipal Long userId,
            @PathVariable UUID productId,
            @Valid @RequestBody PatchProductHiddenReqDto requestDto
    ) {
        UpdateProductHiddenResult result = productService.updateProductHidden(
                userId,
                productId,
                requestDto.getHidden()
        );

        return ResponseEntity.ok(new PatchProductHiddenResDto(result));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<DeleteProductResDto> deleteProduct(
            @AuthenticationPrincipal Long userId,
            @PathVariable UUID productId
    ) {
        DeleteProductResult result = productService.deleteProduct(userId, productId);
        return ResponseEntity.ok(new DeleteProductResDto(result));
    }

    @GetMapping("/admin/products")
    public ResponseEntity<GetAdminProductsPageResDto> getAdminProducts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean includeHidden,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted
    ) {
        // 추후 관리자 권한 검증 추가
        Pageable pageable = PageRequest.of(page, normalizeSize(size), Sort.by(Sort.Direction.DESC, "updatedAt"));

        GetAdminProductsPageQuery query = GetAdminProductsPageQuery.from(
                storeId,
                keyword,
                includeHidden,
                includeDeleted
        );

        GetAdminProductsPageResult result = productService.getAdminProducts(query, pageable);
        return ResponseEntity.ok(GetAdminProductsPageResDto.from(result));
    }

    private int normalizeSize(int size) {
        if (size == 10 || size == 30 || size == 50) {
            return size;
        }
        return 10;
    }
}