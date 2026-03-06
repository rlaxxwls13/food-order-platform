package nbcamp.food_order_platform.product.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.product.application.dto.command.CreateProductCommand;
import nbcamp.food_order_platform.product.application.dto.result.CreateProductResult;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public CreateProductResult createProduct(CreateProductCommand productDto) {
        //validateOwner(storeId, userId); 가게 주인 확인

        /*
        useAi = true 설명 생성 요청
        useAi = false 입력한 설명 그대로 저장
         */
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


        //ai log 저장

        return new CreateProductResult(
                saved.getStoreId(),
                saved.getId(),
                saved.getName(),
                saved.getQuantity(),
                saved.getPrice(),
                saved.getDescription(),
                saved.isHidden(),
                saved.getCreatedAt()
        );
    }


//    public void validateOwner(UUID storeId, Long userId){ //가게 주인 확인
//        boolean isOwner =
//        if (!isOwner) {
//            throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
//        }
//    }
}
