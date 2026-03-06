package nbcamp.food_order_platform.product.repository;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.product.domain.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager em;

    @DisplayName("storeId로 조회하면(기본 메서드) 삭제되지 않은 상품만 반환한다(@SQLRestriction 적용)")
    @Test
    void findAllByStoreId_excludes_deleted() {
        // given
        UUID storeId = UUID.randomUUID();

        Product product1 = new Product(storeId, "p1", "d1", 10, 1000);
        Product product2 = new Product(storeId, "p2", "d2", 20, 2000);
        Product deleted = new Product(storeId, "p3", "d3", 30, 3000);

        productRepository.saveAll(List.of(product1, product2, deleted));
        em.flush();
        em.clear();

        // deleted 처리 (softDelete + save)
        Product toDelete = productRepository.findById(deleted.getId()).orElseThrow();
        toDelete.softDelete(1L);
        productRepository.save(toDelete);
        em.flush();
        em.clear();

        // when
        List<Product> result = productRepository.findAllByStoreId(storeId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("p1", "p2");
    }

    @DisplayName("storeId로 고객용 조회하면(숨김 제외) 삭제되지 않고 숨김이 아닌 상품만 반환한다")
    @Test
    void findAllByStoreIdAndIsHiddenFalse_excludes_hidden_and_deleted() {
        UUID storeId = UUID.randomUUID();

        Product visible1 = new Product(storeId, "p1", "d1", 10, 1000);
        Product visible2 = new Product(storeId, "p2", "d2", 20, 2000);
        Product hidden = new Product(storeId, "p3", "d3", 30, 3000);
        Product deleted = new Product(storeId, "p4", "d4", 40, 4000);

        productRepository.saveAll(List.of(visible1, visible2, hidden, deleted));

        // hide
        Product toHide = productRepository.findById(hidden.getId()).orElseThrow();
        toHide.hide();

        // soft delete
        Product toDelete = productRepository.findById(deleted.getId()).orElseThrow();
        toDelete.softDelete(1L);

        List<Product> result = productRepository.findAllByStoreIdAndIsHiddenFalse(storeId);

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Product::getName)
                        .containsExactlyInAnyOrder("p1", "p2")
        );
    }

    @DisplayName("productId+storeId 단건 조회도 삭제된 데이터는 조회되지 않는다(@SQLRestriction 적용)")
    @Test
    void findByIdAndStoreId_excludes_deleted() {
        // given
        UUID storeId = UUID.randomUUID();
        Product product = productRepository.save(new Product(storeId, "p1", "d1", 10, 1000));
        em.flush();
        em.clear();

        // soft delete
        Product managed = productRepository.findById(product.getId()).orElseThrow();
        managed.softDelete(1L);
        productRepository.save(managed);
        em.flush();
        em.clear();

        // when
        Optional<Product> result = productRepository.findByIdAndStoreId(product.getId(), storeId);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("productId+storeId 고객용 단건 조회는 숨김/삭제 상품을 조회하지 않는다")
    @Test
    void findByIdAndStoreIdAndIsHiddenFalse_excludes_hidden_and_deleted() {
        UUID storeId = UUID.randomUUID();
        Product saved = productRepository.save(new Product(storeId, "p1", "d1", 10, 1000));

        Product managed = productRepository.findById(saved.getId()).orElseThrow();
        managed.hide();

        Optional<Product> result = productRepository.findByIdAndStoreIdAndIsHiddenFalse(saved.getId(), storeId);
        assertThat(result).isEmpty();
    }

    @DisplayName("네이티브 쿼리로 storeId 조회 시 삭제 여부와 상관없이 모두 조회된다(IncludingDeleted)")
    @Test
    void findAllByStoreIdIncludingDeleted_returns_all_rows() {
        // given
        UUID storeId = UUID.randomUUID();

        Product alive = new Product(storeId, "p1", "d1", 10, 1000);
        Product deleted = new Product(storeId, "p2", "d2", 10, 1000);

        productRepository.saveAll(List.of(alive, deleted));
        em.flush();
        em.clear();

        // soft delete
        Product toDelete = productRepository.findById(deleted.getId()).orElseThrow();
        toDelete.softDelete(1L);
        productRepository.save(toDelete);
        em.flush();
        em.clear();

        // when
        List<Product> result = productRepository.findAllByStoreIdIncludingDeleted(storeId);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("p1", "p2")
        );
    }

    @DisplayName("네이티브 쿼리로 deleted_at IS NOT NULL 조건이면 삭제된 상품만 조회된다")
    @Test
    void findAllDeletedByStoreId_returns_only_deleted() {
        // given
        UUID storeId = UUID.randomUUID();

        Product alive = new Product(storeId, "p1", "d1", 10, 1000);
        Product deleted1 = new Product(storeId, "p2", "d2", 10, 1000);
        Product deleted2 = new Product(storeId, "p3", "d3", 10, 1000);

        productRepository.saveAll(List.of(alive, deleted1, deleted2));
        em.flush();
        em.clear();

        // soft delete 2개
        Product d1 = productRepository.findById(deleted1.getId()).orElseThrow();
        d1.softDelete(10L);
        productRepository.save(d1);

        Product d2 = productRepository.findById(deleted2.getId()).orElseThrow();
        d2.softDelete(11L);
        productRepository.save(d2);

        em.flush();
        em.clear();

        // when
        List<Product> result = productRepository.findAllDeletedByStoreId(storeId);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).allMatch(Product::isDeleted),
                () -> assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("p2", "p3")
        );
    }

    @DisplayName("@Version은 저장 시 null이 아니고, 업데이트 시 값이 증가한다")
    @Test
    void version_is_initialized_and_increments_on_update() {
        // given
        UUID storeId = UUID.randomUUID();
        Product saved = productRepository.save(new Product(storeId, "p1", "d1", 10, 1000));
        em.flush();
        em.clear();

        Product p1 = productRepository.findById(saved.getId()).orElseThrow();
        Long v1 = p1.getVersion();

        // when
        p1.changePrice(2000);
        productRepository.save(p1);
        em.flush();
        em.clear();

        Product p2 = productRepository.findById(saved.getId()).orElseThrow();
        Long v2 = p2.getVersion();

        // then
        assertThat(v1).isNotNull();
        assertThat(v2).isNotNull();
        assertThat(v2).isGreaterThan(v1);
    }

    @DisplayName("hide/unhide: 숨김 처리/해제 시 isHidden 값이 변경된다")
    @Test
    void hide_and_unhide() {
        Product product = new Product(UUID.randomUUID(), "name", "desc", 10, 1000);

        product.hide();
        boolean hiddenAfterHide = product.isHidden();

        product.unhide();
        boolean hiddenAfterUnhide = product.isHidden();

        assertAll(
                () -> assertThat(hiddenAfterHide).isTrue(),
                () -> assertThat(hiddenAfterUnhide).isFalse()
        );
    }

    @DisplayName("changeName/changeDescription: 정상 값이면 변경된다")
    @Test
    void change_name_and_description_success() {
        Product product = new Product(UUID.randomUUID(), "name", "desc", 10, 1000);

        product.changeName("new-name");
        product.changeDescription("new-desc");

        assertAll(
                () -> assertThat(product.getName()).isEqualTo("new-name"),
                () -> assertThat(product.getDescription()).isEqualTo("new-desc")
        );
    }

    @DisplayName("changeName/changeDescription: null/blank 또는 최대 길이 초과면 예외")
    @Test
    void change_name_and_description_validation_fail() {
        Product product = new Product(UUID.randomUUID(), "name", "desc", 10, 1000);

        assertAll(
                () -> assertThatThrownBy(() -> product.changeName("   "))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> product.changeName("a".repeat(51)))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> product.changeDescription(null))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> product.changeDescription("a".repeat(101)))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @DisplayName("changeQuantity/changePrice: 0 이상이면 변경되고, 음수면 예외")
    @Test
    void change_quantity_and_price_validation() {
        Product product = new Product(UUID.randomUUID(), "name", "desc", 10, 1000);

        product.changeQuantity(0);
        product.changePrice(0);

        assertAll(
                () -> assertThat(product.getQuantity()).isEqualTo(0),
                () -> assertThat(product.getPrice()).isEqualTo(0),
                () -> assertThatThrownBy(() -> product.changeQuantity(-1))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> product.changePrice(-1))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @DisplayName("increaseStock/decreaseStock: 정상 처리되며, 0 이하/재고 부족이면 예외")
    @Test
    void increase_and_decrease_stock_validation() {
        Product product = new Product(UUID.randomUUID(), "name", "desc", 10, 1000);

        product.increaseStock(5);
        int afterIncrease = product.getQuantity();

        product.decreaseStock(3);
        int afterDecrease = product.getQuantity();

        assertAll(
                () -> assertThat(afterIncrease).isEqualTo(15),
                () -> assertThat(afterDecrease).isEqualTo(12),
                () -> assertThatThrownBy(() -> product.increaseStock(0))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> product.decreaseStock(0))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> product.decreaseStock(999))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }
}