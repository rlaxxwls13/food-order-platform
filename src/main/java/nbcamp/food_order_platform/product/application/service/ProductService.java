package nbcamp.food_order_platform.product.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.ai.application.dto.command.CreateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.service.AiDescriptionService;
import nbcamp.food_order_platform.ai.application.service.AiLogService;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.product.application.dto.command.*;
import nbcamp.food_order_platform.product.application.dto.command.UpdateProductCommand;
import nbcamp.food_order_platform.product.application.dto.query.GetAdminProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.query.GetProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.result.*;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final AiDescriptionService aiDescriptionService;
    private final AiLogService aiLogService;

    @Transactional
    public CreateProductResult createProduct(CreateProductCommand productDto, Long userId, Role role) {
        validateStorePermission(productDto.getStoreId(), userId, role);

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
    public UpdateProductResult updateProduct(UpdateProductCommand productDto, Long userId, Role role) {
        Product product = productRepository.findById(productDto.getProductId())
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        validateStorePermission(product.getStoreId(), userId, role);

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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
    public UpdateProductHiddenResult updateProductHidden(UUID productId, boolean hidden, Long userId, Role role) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        validateStorePermission(product.getStoreId(), userId, role);

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
    public DeleteProductResult deleteProduct(UUID productId, Long userId, Role role) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        validateStorePermission(product.getStoreId(), userId, role);

        product.softDelete(userId);

        return new DeleteProductResult(
                product.getId(),
                product.getDeletedAt(),
                product.getDeletedBy()
        );
    }

    @Transactional(readOnly = true)
    public GetAdminProductsPageResult getAdminProducts(GetAdminProductsPageQuery query, Pageable pageable, Long userId, Role role) {
        validateStorePermission(query.getStoreId(), userId, role);

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

    public void validateStorePermission(UUID storeId, Long userId, Role role){ //가게 주인 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        if (role == Role.MANAGER || role == Role.MASTER)
            return;

        if (role == Role.OWNER && store.getOwnerId().equals(userId))
            return;

        throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
    }
}
