package nbcamp.food_order_platform.product.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.ai.application.dto.command.CreateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.service.AiDescriptionService;
import nbcamp.food_order_platform.ai.application.service.AiLogService;
import nbcamp.food_order_platform.ai.domain.entity.AiDescription;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.product.application.dto.command.*;
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
    private final AiDescriptionService aiDescriptionService;
    private final AiLogService aiLogService;

    @Transactional
    public CreateProductResult createProduct(CreateProductCommand productDto) {
        //validateOwner(storeId, userId); 가게 주인 확인

        /*
        useAi = true 설명 생성 요청
        useAi = false 입력한 설명 그대로 저장
         */
        String description = productDto.getDescription();
        String original = description;
        if(productDto.isUseAi()){
            description = aiDescriptionService.generateAiDescription(original);
        }

        Product product = new Product(
                productDto.getStoreId(),
                productDto.getName(),
                description,
                productDto.getStockQuantity(),
                productDto.getPrice()
        );

        Product saved = productRepository.save(product);

        if(productDto.isUseAi()){
            CreateAiDescriptionCommand aiDto = new CreateAiDescriptionCommand(
                    saved.getId(),
                    original,
                    description
            );
            aiLogService.createAiLog(aiDto);
        }

        return new CreateProductResult(
                saved.getId(),
                saved.getStoreId(),
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
            String original = description;
            if(productDto.getUseAi()){
                description = aiDescriptionService.generateAiDescription(original);

                CreateAiDescriptionCommand aiDto = new CreateAiDescriptionCommand(
                        productDto.getProductId(),
                        original,
                        description
                );
                aiLogService.createAiLog(aiDto);
            }
            product.changeDescription(description);
        }

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
