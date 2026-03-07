package nbcamp.food_order_platform.product.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.product.application.dto.command.CreateProductCommand;
import nbcamp.food_order_platform.product.application.dto.command.UpdateProductCommand;
import nbcamp.food_order_platform.product.application.dto.query.GetAdminProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.query.GetProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.result.*;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
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

    @Transactional
    public GetProductResult getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        return new GetProductResult(
                product.getId(),
                product.getStoreId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getDescription(),
                product.isHidden(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    @Transactional
    public GetProductsPageResult getProducts(GetProductsPageQuery query, Pageable pageable) {
        Page<Product> productPage = productRepository.searchProducts(
                query.getStoreId(),
                query.getKeyword(),
                pageable
        );

        Page<GetProductsPageResult.ProductSummary> resultPage = productPage.map(product ->
                GetProductsPageResult.ProductSummary.builder()
                        .productId(product.getId())
                        .storeId(product.getStoreId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .stockQuantity(product.getQuantity())
                        .isHidden(product.isHidden())
                        .createdAt(product.getCreatedAt())
                        .build()
        );

        return GetProductsPageResult.from(resultPage);
    }

    @Transactional
    public UpdateProductHiddenResult updateProductHidden(Long userId, UUID productId, boolean hidden) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        // validateOwner(productId, userId); 가게 주인 확인

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        if (hidden) {
            product.hide();
        } else {
            product.unhide();
        }

        return new UpdateProductHiddenResult(
                product.getId(),
                product.isHidden(),
                product.getUpdatedAt()
        );
    }

    @Transactional
    public DeleteProductResult deleteProduct(Long userId, UUID productId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        // validateOwner(productId, userId); 가게 주인 확인

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        product.softDelete(userId);

        return new DeleteProductResult(
                product.getId(),
                product.getDeletedAt(),
                product.getDeletedBy()
        );
    }

    @Transactional
    public GetAdminProductsPageResult getAdminProducts(GetAdminProductsPageQuery query, Pageable pageable) {
        // null 방지 (null => false)
        boolean includeHidden = Boolean.TRUE.equals(query.getIncludeHidden());
        boolean includeDeleted = Boolean.TRUE.equals(query.getIncludeDeleted());

        Page<Product> productPage;

        if (includeDeleted) {
            productPage = productRepository.searchAdminProductsIncludingDeleted(
                    query.getStoreId(),
                    query.getKeyword(),
                    includeHidden,
                    pageable
            );
        } else {
            productPage = productRepository.searchAdminProducts(
                    query.getStoreId(),
                    query.getKeyword(),
                    includeHidden,
                    pageable
            );
        }

        Page<GetAdminProductsPageResult.ProductAdminSummary> resultPage = productPage.map(product ->
                GetAdminProductsPageResult.ProductAdminSummary.builder()
                        .productId(product.getId())
                        .storeId(product.getStoreId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .stockQuantity(product.getQuantity())
                        .isHidden(product.isHidden())
                        .isDeleted(product.getDeletedAt() != null)
                        .updatedAt(product.getUpdatedAt())
                        .build()
        );

        return GetAdminProductsPageResult.from(resultPage);
    }

//    public void validateOwner(UUID storeId, Long userId){ //가게 주인 확인
//        boolean isOwner =
//        if (!isOwner) {
//            throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
//        }
//    }
}
