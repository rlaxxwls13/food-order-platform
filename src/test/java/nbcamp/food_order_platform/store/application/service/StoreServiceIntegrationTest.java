package nbcamp.food_order_platform.store.application.service;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.store.application.dto.common.StoreResult;
import nbcamp.food_order_platform.store.application.dto.request.CreateStoreCommand;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresAdminPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.UpdateStoreCommand;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresAdminPageResult;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresPageResult;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.entity.StoreCategory;
import nbcamp.food_order_platform.store.domain.entity.StoreRegion;
import nbcamp.food_order_platform.store.domain.repository.StoreCategoryRepository;
import nbcamp.food_order_platform.store.domain.repository.StoreRegionRepository;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class StoreServiceIntegrationTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreRegionRepository storeRegionRepository;

    @Autowired
    private StoreCategoryRepository storeCategoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager em;

    @DisplayName("가게 생성 성공")
    @Test
    void createStore_success() {
        // given
        long userId = 1001L;
        seedUser(userId);

        UUID regionCode = UUID.randomUUID();
        seedRegionCode(regionCode, "서울 강남구");

        UUID category1 = UUID.randomUUID();
        UUID category2 = UUID.randomUUID();
        seedCategory(category1, "치킨");
        seedCategory(category2, "피자");

        CreateStoreCommand command = CreateStoreCommand.from(
                "햄버거가게",
                regionCode,
                "테헤란로 123",
                List.of(category1, category2)
        );

        // when
        StoreResult result = storeService.createStore(userId, command);
        flushAndClear();

        // then
        UUID storeId = result.getStore().getId();

        Store savedStore = storeRepository.findById(storeId).orElseThrow();
        StoreRegion savedRegion = storeRegionRepository.findByStoreId(storeId).orElseThrow();
        List<StoreCategory> savedCategories = storeCategoryRepository.findAllByStoreId(storeId);

        assertThat(savedStore.getOwnerId()).isEqualTo(userId);
        assertThat(savedStore.getName()).isEqualTo("햄버거가게");
        assertThat(savedRegion.getDetail()).isEqualTo("테헤란로 123");
        assertThat(savedCategories).hasSize(2);
    }

    @DisplayName("가게 단건 조회 성공")
    @Test
    void getStore_success() {
        // given
        long userId = 1002L;
        seedUser(userId);

        UUID regionCode = UUID.randomUUID();
        seedRegionCode(regionCode, "서울 성동구");

        UUID category1 = UUID.randomUUID();
        UUID category2 = UUID.randomUUID();
        seedCategory(category1, "한식");
        seedCategory(category2, "분식");

        UUID storeId = createStore(
                userId,
                "밥집",
                regionCode,
                "왕십리로 11",
                List.of(category1, category2)
        );

        // when
        StoreResult result = storeService.getStore(storeId);

        // then
        assertThat(result.getStore().getId()).isEqualTo(storeId);
        assertThat(result.getStore().getOwnerId()).isEqualTo(userId);
        assertThat(result.getStore().getName()).isEqualTo("밥집");
        assertThat(result.getStoreRegion().getDetail()).isEqualTo("왕십리로 11");
        assertThat(result.getStoreCategories()).hasSize(2);
    }

    @DisplayName("가게 수정 성공 - 사장, 이름, 지역, 카테고리 변경")
    @Test
    void updateStore_success() {
        // given
        long userId = 1003L;
        seedUser(userId);

        UUID oldRegionCode = UUID.randomUUID();
        UUID newRegionCode = UUID.randomUUID();
        seedRegionCode(oldRegionCode, "서울 중구");
        seedRegionCode(newRegionCode, "서울 송파구");

        UUID category1 = UUID.randomUUID();
        UUID category2 = UUID.randomUUID();
        UUID category3 = UUID.randomUUID();
        seedCategory(category1, "중식");
        seedCategory(category2, "일식");
        seedCategory(category3, "양식");

        UUID storeId = createStore(
                userId,
                "기존가게",
                oldRegionCode,
                "기존주소",
                List.of(category1, category2)
        );

        UpdateStoreCommand command = UpdateStoreCommand.from(
                storeId,
                9999L,
                "변경가게",
                newRegionCode,
                "변경주소",
                List.of(category2, category3)
        );

        // when
        StoreResult result = storeService.updateStore(userId, command);
        flushAndClear();

        // then
        Store updatedStore = storeRepository.findById(storeId).orElseThrow();
        StoreRegion updatedRegion = storeRegionRepository.findByStoreId(storeId).orElseThrow();
        List<StoreCategory> activeCategories = storeCategoryRepository.findAllByStoreId(storeId);

        List<UUID> activeCategoryIds = activeCategories.stream()
                .map(sc -> sc.getCategory().getId())
                .sorted(Comparator.naturalOrder())
                .toList();

        Long rawStoreCategoryRowCount = jdbcTemplate.queryForObject(
                "select count(*) from p_store_category where store_id = ?",
                Long.class,
                storeId
        );

        assertThat(result.getStore().getId()).isEqualTo(storeId);
        assertThat(updatedStore.getOwnerId()).isEqualTo(9999L);
        assertThat(updatedStore.getName()).isEqualTo("변경가게");
        assertThat(updatedRegion.getRegionCode().getCode()).isEqualTo(newRegionCode);
        assertThat(updatedRegion.getDetail()).isEqualTo("변경주소");

        assertThat(activeCategories).hasSize(2);
        assertThat(activeCategoryIds).containsExactly(
                Stream.of(category2, category3).sorted().toArray(UUID[]::new)
        );

        // category1은 soft delete 되고, category3은 새로 추가되므로
        // 실제 테이블 row는 총 3개여야 함
        assertThat(rawStoreCategoryRowCount).isEqualTo(3L);
    }

    @DisplayName("가게 삭제 성공 - 일반 조회에서는 제외되고 관리자 삭제 포함 조회에서는 노출")
    @Test
    void deleteStore_success_and_visible_only_in_admin_include_deleted() {
        // given
        long userId = 1004L;
        seedUser(userId);

        UUID regionCode = UUID.randomUUID();
        seedRegionCode(regionCode, "경기 안산시");

        UUID categoryId = UUID.randomUUID();
        seedCategory(categoryId, "카페");

        UUID storeId = createStore(
                userId,
                "삭제대상가게",
                regionCode,
                "중앙대로 1",
                List.of(categoryId)
        );

        // when
        StoreResult deleted = storeService.deleteStore(userId, storeId);
        flushAndClear();

        // then
        assertThat(deleted.getStore().getId()).isEqualTo(storeId);
        assertThat(storeRepository.findById(storeId)).isEmpty();

        Store deletedStore = storeRepository.findByIdIncludingDeleted(storeId).orElseThrow();
        assertThat(deletedStore.getDeletedAt()).isNotNull();
        assertThat(deletedStore.getDeletedBy()).isEqualTo(userId);

        Pageable pageable = PageRequest.of(0, 10);

        GetStoresPageResult normalResult = storeService.getStores(
                GetStoresPageQuery.from(null, null, "삭제대상가게"),
                pageable
        );

        GetStoresAdminPageResult adminWithoutDeleted = storeService.getAdminStores(
                GetStoresAdminPageQuery.from(null, null, "삭제대상가게", false),
                pageable
        );

        GetStoresAdminPageResult adminWithDeleted = storeService.getAdminStores(
                GetStoresAdminPageQuery.from(null, null, "삭제대상가게", true),
                pageable
        );

        assertThat(normalResult.getTotalElements()).isZero();
        assertThat(adminWithoutDeleted.getTotalElements()).isZero();
        assertThat(adminWithDeleted.getTotalElements()).isEqualTo(1);
        assertThat(adminWithDeleted.getContent()).hasSize(1);
        assertThat(adminWithDeleted.getContent().get(0).getStore().getId()).isEqualTo(storeId);
        assertThat(adminWithDeleted.getContent().get(0).getStoreRegion()).isNotNull();
        assertThat(adminWithDeleted.getContent().get(0).getStoreCategories()).hasSize(1);
    }

    @DisplayName("일반 목록 조회 성공 - region/category/storeName 필터 적용")
    @Test
    void getStores_success_with_filters() {
        // given
        long userId = 1005L;
        seedUser(userId);

        UUID regionA = UUID.randomUUID();
        UUID regionB = UUID.randomUUID();
        seedRegionCode(regionA, "서울 마포구");
        seedRegionCode(regionB, "서울 서초구");

        UUID chicken = UUID.randomUUID();
        UUID pizza = UUID.randomUUID();
        seedCategory(chicken, "치킨");
        seedCategory(pizza, "피자");

        createStore(userId, "햄치킨", regionA, "주소1", List.of(chicken));
        createStore(userId, "햄피자", regionB, "주소2", List.of(pizza));
        createStore(userId, "일반식당", regionA, "주소3", List.of(pizza));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        GetStoresPageResult result = storeService.getStores(
                GetStoresPageQuery.from(regionA, chicken, "햄"),
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStore().getName()).isEqualTo("햄치킨");
    }

    private UUID createStore(
            Long userId,
            String name,
            UUID regionCode,
            String addressDetail,
            List<UUID> categoryIds
    ) {
        CreateStoreCommand command = CreateStoreCommand.from(
                name,
                regionCode,
                addressDetail,
                categoryIds
        );
        StoreResult result = storeService.createStore(userId, command);
        flushAndClear();
        return result.getStore().getId();
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

    private void seedRegionCode(UUID regionCode, String regionName) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update("""
                insert into p_region_code (
                    region_code, region_name, is_active,
                    created_at, created_by, updated_at, updated_by
                ) values (?, ?, ?, ?, ?, ?, ?)
                """,
                regionCode,
                regionName,
                true,
                now,
                0L,
                now,
                0L
        );
    }

    private void seedCategory(UUID categoryId, String name) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update("""
                insert into p_category (
                    category_id, name,
                    created_at, created_by, updated_at, updated_by
                ) values (?, ?, ?, ?, ?, ?)
                """,
                categoryId,
                name,
                now,
                0L,
                now,
                0L
        );
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
