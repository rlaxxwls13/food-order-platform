package nbcamp.food_order_platform.product.application;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.product.application.dto.command.CreateProductCommand;
import nbcamp.food_order_platform.product.application.dto.command.UpdateProductCommand;
import nbcamp.food_order_platform.product.application.dto.query.GetAdminProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.query.GetProductsPageQuery;
import nbcamp.food_order_platform.product.application.dto.result.*;
import nbcamp.food_order_platform.product.application.service.ProductService;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager em;

    @DisplayName("상품 생성 성공")
    @Test
    void createProduct_success() {
        // given
        long userId = 2001L;
        seedUser(userId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, userId, "테스트가게");

        CreateProductCommand command = new CreateProductCommand(
                storeId,
                "콜라",
                10,
                2000,
                "탄산음료",
                false
        );

        // when
        CreateProductResult result = productService.createProduct(command);
        flushAndClear();

        // then
        Product saved = productRepository.findById(result.getProductId()).orElseThrow();
        assertThat(saved.getStoreId()).isEqualTo(storeId);
        assertThat(saved.getName()).isEqualTo("콜라");
        assertThat(saved.getQuantity()).isEqualTo(10);
        assertThat(saved.getPrice()).isEqualTo(2000);
        assertThat(saved.getDescription()).isEqualTo("탄산음료");
        assertThat(saved.isHidden()).isFalse();
    }

    @DisplayName("상품 단건 조회 성공")
    @Test
    void getProduct_success() {
        // given
        long userId = 2002L;
        seedUser(userId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, userId, "조회가게");

        UUID productId = createProduct(storeId, "사이다", 15, 1800, "음료");

        // when
        GetProductResult result = productService.getProduct(productId);

        // then
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getStoreId()).isEqualTo(storeId);
        assertThat(result.getName()).isEqualTo("사이다");
        assertThat(result.getStockQuantity()).isEqualTo(15);
        assertThat(result.getPrice()).isEqualTo(1800);
    }

    @DisplayName("상품 수정 성공")
    @Test
    void updateProduct_success() {
        // given
        long userId = 2003L;
        seedUser(userId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, userId, "수정가게");

        UUID productId = createProduct(storeId, "기존상품", 10, 1000, "기존설명");

        UpdateProductCommand command = new UpdateProductCommand(
                productId,
                "변경상품",
                "변경설명",
                5,
                30,
                3000,
                false
        );

        // when
        UpdateProductResult result = productService.updateProduct(command);
        flushAndClear();

        // then
        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(updated.getName()).isEqualTo("변경상품");
        assertThat(updated.getDescription()).isEqualTo("변경설명");
        assertThat(updated.getQuantity()).isEqualTo(30);
        assertThat(updated.getPrice()).isEqualTo(3000);
    }

    @DisplayName("일반 상품 목록 조회 성공 - storeId, keyword 필터 적용")
    @Test
    void getProducts_success_with_filters() {
        // given
        long userId = 2004L;
        seedUser(userId);

        UUID storeId1 = UUID.randomUUID();
        UUID storeId2 = UUID.randomUUID();
        seedStore(storeId1, userId, "가게1");
        seedStore(storeId2, userId, "가게2");

        createProduct(storeId1, "콜라", 10, 2000, "음료");
        createProduct(storeId1, "사이다", 10, 1800, "음료");
        createProduct(storeId2, "콜드브루", 10, 4500, "커피");

        Pageable pageable = PageRequest.of(0, 10);

        // when
        GetProductsPageResult result = productService.getProducts(
                GetProductsPageQuery.from(storeId1, "콜"),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStoreId()).isEqualTo(storeId1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("콜라");
    }

    @DisplayName("상품 숨김 처리 성공")
    @Test
    void updateProductHidden_success() {
        // given
        long userId = 2005L;
        seedUser(userId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, userId, "숨김가게");

        UUID productId = createProduct(storeId, "숨김상품", 10, 1000, "설명");

        // when
        UpdateProductHiddenResult result = productService.updateProductHidden(userId, productId, true);
        flushAndClear();

        // then
        Product hiddenProduct = productRepository.findById(productId).orElseThrow();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(hiddenProduct.isHidden()).isTrue();
    }

    @DisplayName("상품 삭제 성공 - 일반 조회에서는 제외되고 관리자 삭제 포함 조회에서는 노출")
    @Test
    void deleteProduct_success_and_visible_only_in_admin_include_deleted() {
        // given
        long userId = 2006L;
        seedUser(userId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, userId, "삭제가게");

        UUID productId = createProduct(storeId, "삭제상품", 10, 1000, "설명");

        // when
        DeleteProductResult deleted = productService.deleteProduct(userId, productId);
        flushAndClear();

        // then
        assertThat(deleted.getProductId()).isEqualTo(productId);
        assertThat(productRepository.findById(productId)).isEmpty();

        Product deletedProduct = productRepository.findByIdIncludingDeleted(productId).orElseThrow();
        assertThat(deletedProduct.getDeletedAt()).isNotNull();
        assertThat(deletedProduct.getDeletedBy()).isEqualTo(userId);

        Pageable pageable = PageRequest.of(0, 10);

        GetProductsPageResult normalResult = productService.getProducts(
                GetProductsPageQuery.from(storeId, "삭제상품"),
                pageable
        );

        GetAdminProductsPageResult adminWithoutDeleted = productService.getAdminProducts(
                GetAdminProductsPageQuery.from(storeId, "삭제상품", true, false),
                pageable
        );

        GetAdminProductsPageResult adminWithDeleted = productService.getAdminProducts(
                GetAdminProductsPageQuery.from(storeId, "삭제상품", true, true),
                pageable
        );

        assertThat(normalResult.getTotalElements()).isZero();
        assertThat(adminWithoutDeleted.getTotalElements()).isZero();
        assertThat(adminWithDeleted.getTotalElements()).isEqualTo(1);
        assertThat(adminWithDeleted.getContent()).hasSize(1);
        assertThat(adminWithDeleted.getContent().get(0).getProductId()).isEqualTo(productId);
        assertThat(adminWithDeleted.getContent().get(0).isDeleted()).isTrue();
    }

    @DisplayName("관리자 상품 목록 조회 성공 - 숨김/삭제 포함")
    @Test
    void getAdminProducts_success_with_hidden_and_deleted() {
        // given
        long userId = 2007L;
        seedUser(userId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, userId, "관리자가게");

        UUID activeId = createProduct(storeId, "일반상품", 10, 1000, "설명");
        UUID hiddenId = createProduct(storeId, "숨김상품", 10, 2000, "설명");
        UUID deletedId = createProduct(storeId, "삭제상품", 10, 3000, "설명");

        productService.updateProductHidden(userId, hiddenId, true);
        productService.deleteProduct(userId, deletedId);
        flushAndClear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        GetAdminProductsPageResult result = productService.getAdminProducts(
                GetAdminProductsPageQuery.from(storeId, null, true, true),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().stream().anyMatch(p -> p.getProductId().equals(activeId) && !p.isDeleted())).isTrue();
        assertThat(result.getContent().stream().anyMatch(p -> p.getProductId().equals(hiddenId) && p.isHidden())).isTrue();
        assertThat(result.getContent().stream().anyMatch(p -> p.getProductId().equals(deletedId) && p.isDeleted())).isTrue();
    }

    private UUID createProduct(UUID storeId, String name, int quantity, int price, String description) {
        CreateProductCommand command = new CreateProductCommand(
                storeId,
                name,
                quantity,
                price,
                description,
                false
        );
        CreateProductResult result = productService.createProduct(command);
        flushAndClear();
        return result.getProductId();
    }

    private void seedUser(Long userId) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update("""
            insert into p_user (
                user_id, username, nickname, email, password, role,
                created_at, created_by, updated_at, updated_by
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                userId,
                "user" + userId,
                "nickname" + userId,
                "user" + userId + "@test.com",
                "password",
                "OWNER",
                now,
                userId,
                now,
                userId
        );
    }

    private void seedStore(UUID storeId, Long ownerId, String name) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update("""
            insert into p_store (
                store_id, owner_id, name, total_rating_sum, review_count, version,
                created_at, created_by, updated_at, updated_by
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                storeId,
                ownerId,
                name,
                0,
                0,
                0L,
                now,
                ownerId,
                now,
                ownerId
        );
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
