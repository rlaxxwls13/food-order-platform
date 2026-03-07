package nbcamp.food_order_platform.product.application;

import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.product.application.dto.command.UpdateProductCommand;
import nbcamp.food_order_platform.product.application.dto.query.GetAdminProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.query.GetProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.result.*;
import nbcamp.food_order_platform.product.application.service.ProductService;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    @DisplayName("상품 단건 조회 성공")
    @Test
    void getProduct_success() {
        // given
        UUID productId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        Product product = mock(Product.class);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getId()).thenReturn(productId);
        when(product.getStoreId()).thenReturn(storeId);
        when(product.getName()).thenReturn("콜라");
        when(product.getPrice()).thenReturn(2000);
        when(product.getQuantity()).thenReturn(30);
        when(product.getDescription()).thenReturn("탄산음료");
        when(product.isHidden()).thenReturn(false);
        when(product.getCreatedAt()).thenReturn(createdAt);
        when(product.getUpdatedAt()).thenReturn(updatedAt);

        // when
        GetProductResult result = productService.getProduct(productId);

        // then
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getStoreId()).isEqualTo(storeId);
        assertThat(result.getName()).isEqualTo("콜라");
        assertThat(result.getPrice()).isEqualTo(2000);
        assertThat(result.getStockQuantity()).isEqualTo(30);
        assertThat(result.getDescription()).isEqualTo("탄산음료");
        assertThat(result.isHidden()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @DisplayName("상품 단건 조회 시 상품이 없으면 NOT_EXISTED_PRODUCT 예외가 발생한다")
    @Test
    void getProduct_fails_when_product_not_found() {
        // given
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_PRODUCT);
                });
    }

    @DisplayName("상품 목록 조회 성공")
    @Test
    void getProducts_success() {
        // given
        UUID storeId = UUID.randomUUID();
        GetProductsPageQuery query = GetProductsPageQuery.from(storeId, "콜");
        Pageable pageable = PageRequest.of(0, 10);

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        when(productRepository.searchProducts(storeId, "콜", pageable))
                .thenReturn(new PageImpl<>(List.of(product1, product2), pageable, 2));

        when(product1.getId()).thenReturn(UUID.randomUUID());
        when(product1.getStoreId()).thenReturn(storeId);
        when(product1.getName()).thenReturn("콜라");
        when(product1.getPrice()).thenReturn(2000);
        when(product1.getQuantity()).thenReturn(10);
        when(product1.isHidden()).thenReturn(false);
        when(product1.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(product2.getId()).thenReturn(UUID.randomUUID());
        when(product2.getStoreId()).thenReturn(storeId);
        when(product2.getName()).thenReturn("제로콜라");
        when(product2.getPrice()).thenReturn(2500);
        when(product2.getQuantity()).thenReturn(20);
        when(product2.isHidden()).thenReturn(false);
        when(product2.getCreatedAt()).thenReturn(LocalDateTime.now());

        // when
        GetProductsPageResult result = productService.getProducts(query, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("콜라");
        assertThat(result.getContent().get(0).getStockQuantity()).isEqualTo(10);
        assertThat(result.getContent().get(1).getName()).isEqualTo("제로콜라");
    }

    @DisplayName("상품 수정 성공")
    @Test
    void updateProduct_success() {
        // given
        UUID productId = UUID.randomUUID();

        UpdateProductCommand command = mock(UpdateProductCommand.class);
        when(command.getProductId()).thenReturn(productId);
        when(command.getName()).thenReturn("수정상품");
        when(command.getDescription()).thenReturn("수정설명");
        when(command.getAddStockQuantity()).thenReturn(5);
        when(command.getSetStockQuantity()).thenReturn(20);
        when(command.getPrice()).thenReturn(3000);
        when(command.getUseAi()).thenReturn(false);

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        when(product.getId()).thenReturn(productId);
        when(product.getStoreId()).thenReturn(UUID.randomUUID());
        when(product.getName()).thenReturn("수정상품");
        when(product.getPrice()).thenReturn(3000);
        when(product.getQuantity()).thenReturn(20);
        when(product.getDescription()).thenReturn("수정설명");
        when(product.isHidden()).thenReturn(false);
        when(product.getUpdatedAt()).thenReturn(LocalDateTime.now());

        // when
        UpdateProductResult result = productService.updateProduct(command);

        // then
        verify(product).changeName("수정상품");
        verify(product).changePrice(3000);
        verify(product).increaseStock(5);
        verify(product).changeQuantity(20);
        verify(product).changeDescription("수정설명");

        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("수정상품");
        assertThat(result.getPrice()).isEqualTo(3000);
        assertThat(result.getStockQuantity()).isEqualTo(20);
    }

    @DisplayName("상품 수정 시 상품이 없으면 NOT_EXISTED_PRODUCT 예외가 발생한다")
    @Test
    void updateProduct_fails_when_product_not_found() {
        // given
        UUID productId = UUID.randomUUID();
        UpdateProductCommand command = mock(UpdateProductCommand.class);
        when(command.getProductId()).thenReturn(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> productService.updateProduct(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_PRODUCT);
                });
    }

    @DisplayName("상품 숨김 처리 성공")
    @Test
    void updateProductHidden_success_hide() {
        // given
        Long userId = 1L;
        UUID productId = UUID.randomUUID();
        Product product = mock(Product.class);
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getId()).thenReturn(productId);
        when(product.isHidden()).thenReturn(true);
        when(product.getUpdatedAt()).thenReturn(LocalDateTime.now());

        // when
        UpdateProductHiddenResult result = productService.updateProductHidden(userId, productId, true);

        // then
        verify(product).hide();
        verify(product, never()).unhide();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.isHidden()).isTrue();
    }

    @DisplayName("상품 숨김 해제 성공")
    @Test
    void updateProductHidden_success_unhide() {
        // given
        Long userId = 1L;
        UUID productId = UUID.randomUUID();
        Product product = mock(Product.class);
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getId()).thenReturn(productId);
        when(product.isHidden()).thenReturn(false);
        when(product.getUpdatedAt()).thenReturn(LocalDateTime.now());

        // when
        UpdateProductHiddenResult result = productService.updateProductHidden(userId, productId, false);

        // then
        verify(product, never()).hide();
        verify(product).unhide();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.isHidden()).isFalse();
    }

    @DisplayName("상품 삭제 성공")
    @Test
    void deleteProduct_success() {
        // given
        Long userId = 1L;
        UUID productId = UUID.randomUUID();

        User user = mock(User.class);
        Product product = mock(Product.class);
        LocalDateTime deletedAt = LocalDateTime.now();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getId()).thenReturn(productId);
        when(product.getDeletedAt()).thenReturn(deletedAt);
        when(product.getDeletedBy()).thenReturn(userId);

        // when
        DeleteProductResult result = productService.deleteProduct(userId, productId);

        // then
        verify(product).softDelete(userId);
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(result.getDeletedBy()).isEqualTo(userId);
    }

    @DisplayName("상품 삭제 시 유저가 없으면 NOT_EXISTED_USER 예외가 발생한다")
    @Test
    void deleteProduct_fails_when_user_not_found() {
        // given
        Long userId = 1L;
        UUID productId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> productService.deleteProduct(userId, productId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_USER);
                });

        verifyNoInteractions(productRepository);
    }

    @DisplayName("관리자 상품 목록 조회 성공 - 삭제 제외")
    @Test
    void getAdminProducts_success_without_deleted() {
        // given
        UUID storeId = UUID.randomUUID();
        GetAdminProductsPageQuery query = GetAdminProductsPageQuery.from(storeId, "콜", true, false);
        Pageable pageable = PageRequest.of(0, 10);

        Product product = mock(Product.class);
        when(productRepository.searchAdminProducts(storeId, "콜", true, pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getStoreId()).thenReturn(storeId);
        when(product.getName()).thenReturn("콜라");
        when(product.getPrice()).thenReturn(2000);
        when(product.getQuantity()).thenReturn(10);
        when(product.isHidden()).thenReturn(true);
        when(product.getDeletedAt()).thenReturn(null);
        when(product.getUpdatedAt()).thenReturn(LocalDateTime.now());

        // when
        GetAdminProductsPageResult result = productService.getAdminProducts(query, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isHidden()).isTrue();
        assertThat(result.getContent().get(0).isDeleted()).isFalse();

        verify(productRepository).searchAdminProducts(storeId, "콜", true, pageable);
        verify(productRepository, never()).searchAdminProductsIncludingDeleted(any(), any(), anyBoolean(), any());
    }

    @DisplayName("관리자 상품 목록 조회 성공 - 삭제 포함")
    @Test
    void getAdminProducts_success_with_deleted() {
        // given
        UUID storeId = UUID.randomUUID();
        GetAdminProductsPageQuery query = GetAdminProductsPageQuery.from(storeId, "콜", true, true);
        Pageable pageable = PageRequest.of(0, 10);

        Product deletedProduct = mock(Product.class);
        Product activeProduct = mock(Product.class);

        when(productRepository.searchAdminProductsIncludingDeleted(storeId, "콜", true, pageable))
                .thenReturn(new PageImpl<>(List.of(deletedProduct, activeProduct), pageable, 2));

        when(deletedProduct.getId()).thenReturn(UUID.randomUUID());
        when(deletedProduct.getStoreId()).thenReturn(storeId);
        when(deletedProduct.getName()).thenReturn("삭제콜라");
        when(deletedProduct.getPrice()).thenReturn(2000);
        when(deletedProduct.getQuantity()).thenReturn(10);
        when(deletedProduct.isHidden()).thenReturn(true);
        when(deletedProduct.getDeletedAt()).thenReturn(LocalDateTime.now());
        when(deletedProduct.getUpdatedAt()).thenReturn(LocalDateTime.now());

        when(activeProduct.getId()).thenReturn(UUID.randomUUID());
        when(activeProduct.getStoreId()).thenReturn(storeId);
        when(activeProduct.getName()).thenReturn("일반콜라");
        when(activeProduct.getPrice()).thenReturn(2500);
        when(activeProduct.getQuantity()).thenReturn(20);
        when(activeProduct.isHidden()).thenReturn(false);
        when(activeProduct.getDeletedAt()).thenReturn(null);
        when(activeProduct.getUpdatedAt()).thenReturn(LocalDateTime.now());

        // when
        GetAdminProductsPageResult result = productService.getAdminProducts(query, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).isDeleted()).isTrue();
        assertThat(result.getContent().get(1).isDeleted()).isFalse();

        verify(productRepository).searchAdminProductsIncludingDeleted(storeId, "콜", true, pageable);
        verify(productRepository, never()).searchAdminProducts(any(), any(), anyBoolean(), any());
    }
}