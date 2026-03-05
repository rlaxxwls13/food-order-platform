package nbcamp.food_order_platform.product.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.product.application.dto.CreateProductDto;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public CreateProductDto createProduct(CreateProductDto productDto) {
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

        Product saved = productRepository.save(product);

        return new CreateProductDto(
                saved.getStoreId(),
                saved.getName(),
                saved.getQuantity(),
                saved.getPrice(),
                saved.getDescription(),
                saved.getId(),
                saved.isHidden(),
                saved.getCreatedAt()
        );
    }

    public boolean isOwner(UUID storeId){ //가게 주인 확인
    // store.getOwnerId = userId 비교
        return true;
    }
}
