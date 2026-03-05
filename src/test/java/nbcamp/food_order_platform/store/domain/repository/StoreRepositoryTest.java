package nbcamp.food_order_platform.store.domain.repository;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.category.domain.entity.Category;
import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import nbcamp.food_order_platform.store.domain.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StoreRepositoryTest {

    private static final String STORE_CATEGORY_TABLE = "p_store_category";
    private static final String STORE_REGION_TABLE = "p_store_region";

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = persistCategory("c1");
        category2 = persistCategory("c2");
        flushAndClear();
    }

    @DisplayName("기본 findAll 조회는 삭제되지 않은 가게만 반환한다(@SQLRestriction 적용)")
    @Test
    void findAll_excludes_deleted() {
        RegionCode rc = persistRegionCode("서울", true);

        Store store1 = new Store(1L, "s1", rc, "detail-1", List.of());
        Store store2 = new Store(2L, "s2", rc, "detail-2", List.of());
        Store deleted = new Store(3L, "s3", rc, "detail-3", List.of());

        storeRepository.saveAll(List.of(store1, store2, deleted));
        flushAndClear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(deleted.getId()).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        flushAndClear();

        List<Store> result = storeRepository.findAll();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Store::getName)
                        .containsExactlyInAnyOrder("s1", "s2")
        );
    }

    @DisplayName("기본 findById 조회는 삭제된 가게를 조회하지 않는다(@SQLRestriction 적용)")
    @Test
    void findById_excludes_deleted() {
        RegionCode rc = persistRegionCode("서울", true);

        Store saved = storeRepository.save(new Store(1L, "s1", rc, "detail", List.of()));
        UUID id = saved.getId();
        flushAndClear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(id).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        flushAndClear();

        Optional<Store> result = storeRepository.findById(id);

        assertThat(result).isEmpty();
    }

    @DisplayName("네이티브 findByIdIncludingDeleted는 삭제된 가게도 조회한다")
    @Test
    void findByIdIncludingDeleted_returns_deleted_too() {
        RegionCode rc = persistRegionCode("서울", true);

        Store saved = storeRepository.save(new Store(1L, "s1", rc, "detail", List.of()));
        UUID id = saved.getId();
        flushAndClear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(id).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        flushAndClear();

        Optional<Store> result = storeRepository.findByIdIncludingDeleted(id);

        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().isDeleted()).isTrue()
        );
    }

    @DisplayName("네이티브 findAllIncludingDeleted는 삭제 여부와 관계없이 모두 조회한다")
    @Test
    void findAllIncludingDeleted_returns_all_rows() {
        RegionCode rc = persistRegionCode("서울", true);

        Store alive = new Store(1L, "alive", rc, "detail-a", List.of());
        Store deleted = new Store(2L, "deleted", rc, "detail-d", List.of());

        storeRepository.saveAll(List.of(alive, deleted));
        flushAndClear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(deleted.getId()).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        flushAndClear();

        List<Store> result = storeRepository.findAllIncludingDeleted();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Store::getName)
                        .containsExactlyInAnyOrder("alive", "deleted")
        );
    }

    @DisplayName("@Version은 저장 시 null이 아니고, 업데이트 시 값이 증가한다")
    @Test
    void version_is_initialized_and_increments_on_update() {
        RegionCode rc = persistRegionCode("서울", true);

        Store saved = storeRepository.save(new Store(1L, "s1", rc, "detail", List.of()));
        UUID id = saved.getId();
        flushAndClear();

        Store s1 = storeRepository.findById(id).orElseThrow();
        Long v1 = s1.getVersion();

        s1.changeName("s1-new");
        storeRepository.save(s1);
        flushAndClear();

        Store s2 = storeRepository.findById(id).orElseThrow();
        Long v2 = s2.getVersion();

        assertAll(
                () -> assertThat(v1).isNotNull(),
                () -> assertThat(v2).isNotNull(),
                () -> assertThat(v2).isGreaterThan(v1)
        );
    }

    @DisplayName("changeOwner/changeRegion/changeName: 정상 값이면 변경된다")
    @Test
    void change_fields_success() {
        RegionCode rc1 = persistRegionCode("서울", true);
        RegionCode rc2 = persistRegionCode("부산", true);

        Store store = new Store(1L, "store", rc1, "detail-1", List.of());

        store.changeOwner(2L);
        store.changeRegion(rc2, "detail-2");
        store.changeName("store-new");

        assertAll(
                () -> assertThat(store.getOwnerId()).isEqualTo(2L),
                () -> assertThat(store.getStoreRegion().getRegionCode().getRegionName()).isEqualTo("부산"),
                () -> assertThat(store.getStoreRegion().getDetail()).isEqualTo("detail-2"),
                () -> assertThat(store.getName()).isEqualTo("store-new")
        );
    }

    @DisplayName("changeOwner/changeRegion/changeName: null/blank/길이 초과면 예외")
    @Test
    void change_fields_validation_fail() {
        RegionCode rc = persistRegionCode("서울", true);
        Store store = new Store(1L, "store", rc, "detail", List.of());

        assertAll(
                () -> assertThatThrownBy(() -> store.changeOwner(null))
                        .isInstanceOf(IllegalArgumentException.class),

                () -> assertThatThrownBy(() -> store.changeRegion(null, "detail"))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> store.changeRegion(rc, null))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> store.changeRegion(rc, "   "))
                        .isInstanceOf(IllegalArgumentException.class),

                () -> assertThatThrownBy(() -> store.changeName("   "))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> store.changeName("a".repeat(256)))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @DisplayName("addNewRating: 1~5 범위만 허용하며 누적 합/카운트가 증가한다")
    @Test
    void addNewRating_increments_sum_and_count_and_validates_range() {
        RegionCode rc = persistRegionCode("서울", true);
        Store store = new Store(1L, "store", rc, "detail", List.of());

        store.addNewRating(5);
        store.addNewRating(3);

        assertAll(
                () -> assertThat(store.getTotalRatingSum()).isEqualTo(8),
                () -> assertThat(store.getReviewCount()).isEqualTo(2),
                () -> assertThatThrownBy(() -> store.addNewRating(0))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> store.addNewRating(6))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @DisplayName("removeRating: 리뷰가 있으면 누적 합/카운트가 감소한다")
    @Test
    void removeRating_decrements_sum_and_count_when_review_exists() {
        RegionCode rc = persistRegionCode("서울", true);
        Store store = new Store(1L, "store", rc, "detail", List.of());

        store.addNewRating(5);
        store.addNewRating(3);

        store.removeRating(5);

        assertAll(
                () -> assertThat(store.getTotalRatingSum()).isEqualTo(3),
                () -> assertThat(store.getReviewCount()).isEqualTo(1)
        );
    }

    @DisplayName("removeRating: 리뷰가 0개면 아무 변화가 없다")
    @Test
    void removeRating_does_nothing_when_review_count_is_zero() {
        RegionCode rc = persistRegionCode("서울", true);
        Store store = new Store(1L, "store", rc, "detail", List.of());

        store.removeRating(5);

        assertAll(
                () -> assertThat(store.getTotalRatingSum()).isEqualTo(0),
                () -> assertThat(store.getReviewCount()).isEqualTo(0)
        );
    }

    @DisplayName("getRating: 리뷰가 없으면 0, 있으면 소수점 첫째자리로 반올림한 평균을 반환한다")
    @Test
    void getRating_returns_rounded_average() {
        RegionCode rc = persistRegionCode("서울", true);
        Store store = new Store(1L, "store", rc, "detail", List.of());

        double r0 = store.getRating();

        store.addNewRating(5);
        store.addNewRating(4);
        store.addNewRating(4);

        double r1 = store.getRating();

        assertAll(
                () -> assertThat(r0).isEqualTo(0.0),
                () -> assertThat(r1).isEqualTo(4.3)
        );
    }

    @DisplayName("Store 저장 시 StoreRegion과 StoreCategory가 함께 저장된다(cascade)")
    @Test
    void save_store_cascades_storeRegion_and_storeCategories() {
        RegionCode rc = persistRegionCode("서울", true);

        Store store = new Store(1L, "store", rc, "detail", List.of(category1, category2));
        UUID storeId = storeRepository.save(store).getId();
        flushAndClear();

        Store reloaded = storeRepository.findByIdIncludingDeleted(storeId).orElseThrow();

        assertAll(
                () -> assertThat(reloaded.getStoreRegion()).isNotNull(),
                () -> assertThat(reloaded.getStoreRegion().getDetail()).isEqualTo("detail"),
                () -> assertThat(reloaded.getStoreRegion().getRegionCode().getRegionName()).isEqualTo("서울"),
                () -> assertThat(reloaded.getStoreCategories()).hasSize(2),
                () -> assertThat(reloaded.getStoreCategories().stream().map(sc -> sc.getCategory().getId()).toList())
                        .containsExactlyInAnyOrder(category1.getId(), category2.getId())
        );
    }

    @DisplayName("addCategory: 신규는 추가되고, 동일 id는 중복 추가되지 않는다")
    @Test
    void addCategory_adds_new_and_ignores_duplicate() {
        RegionCode rc = persistRegionCode("서울", true);

        UUID storeId = storeRepository.save(new Store(1L, "store", rc, "detail", List.of())).getId();
        flushAndClear();

        Store s = storeRepository.findByIdIncludingDeleted(storeId).orElseThrow();

        // when
        s.addCategory(category1);
        s.addCategory(category1); // 동일 객체(=동일 id) 중복
        s.addCategory(category2);

        storeRepository.save(s);
        flushAndClear();

        // then
        Store reloaded = storeRepository.findByIdIncludingDeleted(storeId).orElseThrow();
        assertThat(reloaded.getStoreCategories()).hasSize(2);
        assertThat(reloaded.getStoreCategories().stream().map(sc -> sc.getCategory().getId()).toList())
                .containsExactlyInAnyOrder(category1.getId(), category2.getId());
    }

    @DisplayName("removeCategory: 해당 category(id)의 StoreCategory가 softDelete 된다")
    @Test
    void removeCategory_softDeletes_target_storeCategory() {
        RegionCode rc = persistRegionCode("서울", true);

        UUID storeId = storeRepository.save(new Store(1L, "store", rc, "detail", List.of(category1, category2))).getId();
        flushAndClear();

        Store s = storeRepository.findByIdIncludingDeleted(storeId).orElseThrow();

        // when
        s.removeCategory(category1, 777L);
        storeRepository.save(s);
        flushAndClear();

        // then (native)
        Timestamp deletedAt1 = nativeDeletedAtForStoreCategory(storeId, category1.getId());
        Timestamp deletedAt2 = nativeDeletedAtForStoreCategory(storeId, category2.getId());

        assertAll(
                () -> assertThat(deletedAt1).isNotNull(),
                () -> assertThat(deletedAt2).isNull()
        );
    }

    @DisplayName("Store softDelete 시 StoreRegion과 StoreCategory까지 softDelete 된다")
    @Test
    void softDelete_store_cascades_to_storeRegion_and_storeCategories() {
        RegionCode rc = persistRegionCode("서울", true);

        UUID storeId = storeRepository.save(new Store(1L, "store", rc, "detail", List.of(category1, category2))).getId();
        flushAndClear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(storeId).orElseThrow();

        // when
        toDelete.softDelete(999L);
        storeRepository.save(toDelete);
        flushAndClear();

        Object storeRegionDeletedAt = em.createNativeQuery(
                        "select deleted_at from " + STORE_REGION_TABLE + " where store_id = :storeId")
                .setParameter("storeId", storeId)
                .getSingleResult();

        List<?> storeCategoryDeletedAts = em.createNativeQuery(
                        "select deleted_at from " + STORE_CATEGORY_TABLE + " where store_id = :storeId")
                .setParameter("storeId", storeId)
                .getResultList();

        assertAll(
                () -> assertThat(storeRepository.findById(storeId)).isEmpty(),
                () -> assertThat(storeRepository.findByIdIncludingDeleted(storeId)).isPresent(),
                () -> assertThat(storeRegionDeletedAt).isNotNull(),
                () -> assertThat(storeCategoryDeletedAts).hasSize(2),
                () -> assertThat(storeCategoryDeletedAts).allSatisfy(v -> assertThat(v).isNotNull())
        );
    }

    // helper

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private RegionCode persistRegionCode(String regionName, boolean active) {
        RegionCode rc = RegionCode.create(regionName, active);
        em.persist(rc);
        return rc;
    }

    private Category persistCategory(String name) {
        Category c = Category.create(name);
        em.persist(c);
        return c;
    }

    private Timestamp nativeDeletedAtForStoreCategory(UUID storeId, UUID categoryId) {
        List<?> rows = em.createNativeQuery(
                        "select deleted_at from " + STORE_CATEGORY_TABLE + " where store_id = :storeId and category_id = :catId")
                .setParameter("storeId", storeId)
                .setParameter("catId", categoryId)
                .getResultList();
        return rows.isEmpty() ? null : (Timestamp) rows.get(0);
    }
}