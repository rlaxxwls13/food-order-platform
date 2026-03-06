package nbcamp.food_order_platform.product.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.product.application.dto.command.CreateProductCommand;
import nbcamp.food_order_platform.product.application.dto.command.UpdateProductCommand;
import nbcamp.food_order_platform.product.application.dto.result.CreateProductResult;
import nbcamp.food_order_platform.product.application.dto.result.UpdateProductResult;
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

    @Transactional
    public UpdateProductResult updateProduct(UpdateProductCommand productDto) {
        //validateOwner(productId, userId); 가게 주인 확인

        Product product = productRepository.findById(productDto.getProductId())
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        if(productDto.getName() != null) product.changeName(productDto.getName());

        if(productDto.getPrice() != null) product.changePrice(productDto.getPrice());

        if(productDto.getAddStockQuantity() != null) product.increaseStock(productDto.getAddStockQuantity());

        if(productDto.getSetStockQuantity() != null) product.changeQuantity(productDto.getSetStockQuantity());

        if(productDto.getDescription() != null) {
            String description = productDto.getDescription();
            if(productDto.getUseAi())
                description = "생성된 설명";
            product.changeDescription(description);
        }

        //ai로그저장

        return new UpdateProductResult(
                product.getId(),
                product.getStoreId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getDescription(),
                product.isHidden(),
                product.getUpdatedAt()
        );
    }

//    public void validateOwner(UUID storeId, Long userId){ //가게 주인 확인
//        boolean isOwner =
//        if (!isOwner) {
//            throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
//        }
//    }
}
