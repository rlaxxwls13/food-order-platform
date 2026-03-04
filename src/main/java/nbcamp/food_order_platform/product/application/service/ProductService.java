package nbcamp.food_order_platform.product.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.product.application.dto.CreateProductDto;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.product.presentation.dto.PostProductResDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public ResponseEntity<PostProductResDto> createProduct(CreateProductDto productDto) {
        //isOwner(User.getUserId()); 가게 주인 확인

        String description = productDto.getDescription();
        if(productDto.isUseAi()){
            description = "생성된 설명";
        }

        Product product = new Product(
                productDto.getStoreId(),
                productDto.getName(),
                description,
                productDto.getStockQuantity(),
                productDto.getPrice()
        );

        productRepository.save(product);
        PostProductResDto postProductResDto = new PostProductResDto(product);
        return ResponseEntity.ok(postProductResDto);
    }

    public boolean isOwner(UUID storeId){ //가게 주인 확인
    // store.getOwnerId = userId 비교
        return true;
    }
}
