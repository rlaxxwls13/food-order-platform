package nbcamp.food_order_platform.store.domain.repository;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.store.domain.entity.Store;
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
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @DisplayName("기본 findAll 조회는 삭제되지 않은 가게만 반환한다(@SQLRestriction 적용)")
    @Test
    void findAll_excludes_deleted() {
        // given
        Store store1 = new Store(1L, 10L, "s1");
        Store store2 = new Store(2L, 10L, "s2");
        Store deleted = new Store(3L, 10L, "s3");

        storeRepository.saveAll(List.of(store1, store2, deleted));
        em.flush();
        em.clear();

        // soft delete
        Store toDelete = storeRepository.findByIdIncludingDeleted(deleted.getId()).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        em.flush();
        em.clear();

        // when
        List<Store> result = storeRepository.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Store::getName)
                        .containsExactlyInAnyOrder("s1", "s2")
        );
    }

    @DisplayName("기본 findById 조회는 삭제된 가게를 조회하지 않는다(@SQLRestriction 적용)")
    @Test
    void findById_excludes_deleted() {
        // given
        Store saved = storeRepository.save(new Store(1L, 10L, "s1"));
        UUID id = saved.getId();
        em.flush();
        em.clear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(id).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        em.flush();
        em.clear();

        // when
        Optional<Store> result = storeRepository.findById(id);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("네이티브 findByIdIncludingDeleted는 삭제된 가게도 조회한다")
    @Test
    void findByIdIncludingDeleted_returns_deleted_too() {
        // given
        Store saved = storeRepository.save(new Store(1L, 10L, "s1"));
        UUID id = saved.getId();
        em.flush();
        em.clear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(id).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        em.flush();
        em.clear();

        // when
        Optional<Store> result = storeRepository.findByIdIncludingDeleted(id);

        // then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().isDeleted()).isTrue()
        );
    }

    @DisplayName("네이티브 findAllIncludingDeleted는 삭제 여부와 관계없이 모두 조회한다")
    @Test
    void findAllIncludingDeleted_returns_all_rows() {
        // given
        Store alive = new Store(1L, 10L, "alive");
        Store deleted = new Store(2L, 10L, "deleted");

        storeRepository.saveAll(List.of(alive, deleted));
        em.flush();
        em.clear();

        Store toDelete = storeRepository.findByIdIncludingDeleted(deleted.getId()).orElseThrow();
        toDelete.softDelete(1L);
        storeRepository.save(toDelete);
        em.flush();
        em.clear();

        // when
        List<Store> result = storeRepository.findAllIncludingDeleted();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Store::getName)
                        .containsExactlyInAnyOrder("alive", "deleted")
        );
    }

    @DisplayName("@Version은 저장 시 null이 아니고, 업데이트 시 값이 증가한다")
    @Test
    void version_is_initialized_and_increments_on_update() {
        // given
        Store saved = storeRepository.save(new Store(1L, 10L, "s1"));
        UUID id = saved.getId();
        em.flush();
        em.clear();

        Store s1 = storeRepository.findById(id).orElseThrow();
        Long v1 = s1.getVersion();

        // when
        s1.changeName("s1-new");
        storeRepository.save(s1);
        em.flush();
        em.clear();

        Store s2 = storeRepository.findById(id).orElseThrow();
        Long v2 = s2.getVersion();

        // then
        assertAll(
                () -> assertThat(v1).isNotNull(),
                () -> assertThat(v2).isNotNull(),
                () -> assertThat(v2).isGreaterThan(v1)
        );
    }

    @DisplayName("changeOwner/changeRegion/changeName: 정상 값이면 변경된다")
    @Test
    void change_fields_success() {
        Store store = new Store(1L, 10L, "store");

        store.changeOwner(2L);
        store.changeRegion(20L);
        store.changeName("store-new");

        assertAll(
                () -> assertThat(store.getOwnerId()).isEqualTo(2L),
                () -> assertThat(store.getRegionId()).isEqualTo(20L),
                () -> assertThat(store.getName()).isEqualTo("store-new")
        );
    }

    @DisplayName("changeOwner/changeRegion/changeName: null/blank/길이 초과면 예외")
    @Test
    void change_fields_validation_fail() {
        Store store = new Store(1L, 10L, "store");

        assertAll(
                () -> assertThatThrownBy(() -> store.changeOwner(null))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> store.changeRegion(null))
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
        Store store = new Store(1L, 10L, "store");

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
        Store store = new Store(1L, 10L, "store");

        store.addNewRating(5);
        store.addNewRating(3); // sum=8, count=2

        store.removeRating(5); // sum=3, count=1

        assertAll(
                () -> assertThat(store.getTotalRatingSum()).isEqualTo(3),
                () -> assertThat(store.getReviewCount()).isEqualTo(1)
        );
    }

    @DisplayName("removeRating: 리뷰가 0개면 아무 변화가 없다")
    @Test
    void removeRating_does_nothing_when_review_count_is_zero() {
        Store store = new Store(1L, 10L, "store");

        // 초기값이 sum=0, count=0
        store.removeRating(5);

        assertAll(
                () -> assertThat(store.getTotalRatingSum()).isEqualTo(0),
                () -> assertThat(store.getReviewCount()).isEqualTo(0)
        );
    }

    @DisplayName("getRating: 리뷰가 없으면 0, 있으면 소수점 첫째자리로 반올림한 평균을 반환한다")
    @Test
    void getRating_returns_rounded_average() {
        Store store = new Store(1L, 10L, "store");

        double r0 = store.getRating();

        store.addNewRating(5);
        store.addNewRating(4);
        store.addNewRating(4); // 평균 4.333.. -> 4.3

        double r1 = store.getRating();

        assertAll(
                () -> assertThat(r0).isEqualTo(0.0),
                () -> assertThat(r1).isEqualTo(4.3)
        );
    }
}