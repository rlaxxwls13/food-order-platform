package nbcamp.food_order_platform.product.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.product.application.dto.CreateProductDto;
import nbcamp.food_order_platform.product.application.service.ProductService;
import nbcamp.food_order_platform.product.presentation.dto.request.PostProductReqDto;
import nbcamp.food_order_platform.product.presentation.dto.response.PostProductResDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<PostProductResDto> createProduct(@RequestBody PostProductReqDto requestDto) {
        CreateProductDto productDto = new CreateProductDto(
                requestDto.getStoreId(),
                requestDto.getName(),
                requestDto.getStockQuantity(),
                requestDto.getPrice(),
                requestDto.getDescription(),
                requestDto.isUseAi()
        );

        CreateProductDto result = productService.createProduct(productDto);
        return ResponseEntity.ok(new PostProductResDto(result));
    }
}