package nbcamp.food_order_platform.product.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.product.application.dto.command.CreateProductCommand;
import nbcamp.food_order_platform.product.application.dto.command.UpdateProductCommand;
import nbcamp.food_order_platform.product.application.dto.result.CreateProductResult;
import nbcamp.food_order_platform.product.application.dto.result.UpdateProductResult;
import nbcamp.food_order_platform.product.application.service.ProductService;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.presentation.dto.request.PatchProductReqDto;
import nbcamp.food_order_platform.product.presentation.dto.request.PostProductReqDto;
import nbcamp.food_order_platform.product.presentation.dto.response.PatchProductResDto;
import nbcamp.food_order_platform.product.presentation.dto.response.PostProductResDto;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
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
}