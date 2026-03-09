package nbcamp.food_order_platform.store;

import nbcamp.food_order_platform.global.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Transactional
class StoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId;
    private Long anotherOwnerId;
    private UUID regionCode1;
    private UUID regionCode2;
    private UUID categoryId1;
    private UUID categoryId2;
    private UUID categoryId3;

    @BeforeEach
    void setUp() {
        ownerId = 100L;
        anotherOwnerId = 999L;

        regionCode1 = UUID.randomUUID();
        regionCode2 = UUID.randomUUID();

        categoryId1 = UUID.randomUUID();
        categoryId2 = UUID.randomUUID();
        categoryId3 = UUID.randomUUID();

        seedUser(ownerId, "OWNER");
        seedUser(anotherOwnerId, "OWNER");

        seedRegionCode(regionCode1, "서울 강남구");
        seedRegionCode(regionCode2, "서울 송파구");

        seedCategory(categoryId1, "치킨");
        seedCategory(categoryId2, "피자");
        seedCategory(categoryId3, "한식");
    }

    @DisplayName("가게 등록 성공")
    @Test
    void createStore_success() throws Exception {
        String requestBody = """
                {
                  "name": "햄버거가게",
                  "regionCodeId": "%s",
                  "addressDetail": "테헤란로 123",
                  "categoryIds": ["%s", "%s"]
                }
                """.formatted(regionCode1, categoryId1, categoryId2);

        mockMvc.perform(post("/api/v1/stores")
                        .with(auth(ownerId, "OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.name").value("햄버거가게"))
                .andExpect(jsonPath("$.address.regionCode").value(regionCode1.toString()))
                .andExpect(jsonPath("$.address.regionName").value("서울 강남구"))
                .andExpect(jsonPath("$.address.addressDetail").value("테헤란로 123"))
                .andExpect(jsonPath("$.categories.length()").value(2));
    }

    @DisplayName("가게 단건 조회 성공")
    @Test
    void getStore_success() throws Exception {
        UUID storeId = insertStoreWithRelations(
                ownerId,
                "밥집",
                regionCode1,
                "왕십리로 11",
                List.of(categoryId1, categoryId2)
        );

        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId)
                        .with(auth(ownerId, "OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.name").value("밥집"))
                .andExpect(jsonPath("$.address.regionCode").value(regionCode1.toString()))
                .andExpect(jsonPath("$.address.regionName").value("서울 강남구"))
                .andExpect(jsonPath("$.address.addressDetail").value("왕십리로 11"))
                .andExpect(jsonPath("$.categories.length()").value(2));
    }

    @DisplayName("가게 수정 성공")
    @Test
    void updateStore_success() throws Exception {
        UUID storeId = insertStoreWithRelations(
                ownerId,
                "기존가게",
                regionCode1,
                "기존주소",
                List.of(categoryId1, categoryId2)
        );

        String requestBody = """
                {
                  "ownerId": 7777,
                  "name": "변경가게",
                  "regionCode": "%s",
                  "regionDetail": "변경주소",
                  "categoryIds": ["%s", "%s"]
                }
                """.formatted(regionCode2, categoryId2, categoryId3);

        mockMvc.perform(patch("/api/v1/stores/{storeId}", storeId)
                        .with(auth(ownerId, "OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.ownerId").value(7777))
                .andExpect(jsonPath("$.name").value("변경가게"))
                .andExpect(jsonPath("$.address.regionCode").value(regionCode2.toString()))
                .andExpect(jsonPath("$.address.regionName").value("서울 송파구"))
                .andExpect(jsonPath("$.address.addressDetail").value("변경주소"))
                .andExpect(jsonPath("$.categories.length()").value(2));
    }

    @DisplayName("가게 삭제 성공")
    @Test
    void deleteStore_success() throws Exception {
        UUID storeId = insertStoreWithRelations(
                ownerId,
                "삭제대상가게",
                regionCode1,
                "중앙대로 1",
                List.of(categoryId1)
        );

        mockMvc.perform(delete("/api/v1/stores/{storeId}", storeId)
                        .with(auth(ownerId, "OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.name").value("삭제대상가게"));

        mockMvc.perform(get("/api/v1/stores")
                        .with(auth(ownerId, "OWNER"))
                        .param("storeName", "삭제대상가게")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/api/v1/stores/admin")
                        .with(auth(ownerId, "MANAGER"))
                        .param("storeName", "삭제대상가게")
                        .param("includeDeleted", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].storeId").value(storeId.toString()));
    }

    @DisplayName("일반 가게 목록 조회 성공 - region/category/storeName 필터 적용")
    @Test
    void getStores_success_with_filters() throws Exception {
        insertStoreWithRelations(ownerId, "햄치킨", regionCode1, "주소1", List.of(categoryId1));
        insertStoreWithRelations(ownerId, "햄피자", regionCode2, "주소2", List.of(categoryId2));
        insertStoreWithRelations(ownerId, "일반식당", regionCode1, "주소3", List.of(categoryId3));

        mockMvc.perform(get("/api/v1/stores")
                        .with(auth(ownerId, "OWNER"))
                        .param("regionCode", regionCode1.toString())
                        .param("categoryId", categoryId1.toString())
                        .param("storeName", "햄")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].storeId").exists())
                .andExpect(jsonPath("$.content[0].name").value("햄치킨"));
    }

    @DisplayName("관리자 가게 목록 조회 성공 - 삭제 포함")
    @Test
    void getAdminStores_success_include_deleted() throws Exception {
        UUID activeStoreId = insertStoreWithRelations(
                ownerId,
                "정상가게",
                regionCode1,
                "주소1",
                List.of(categoryId1)
        );

        UUID deletedStoreId = insertStoreWithRelations(
                ownerId,
                "삭제가게",
                regionCode2,
                "주소2",
                List.of(categoryId2)
        );

        softDeleteStore(deletedStoreId, ownerId);

        mockMvc.perform(get("/api/v1/stores/admin")
                        .with(auth(ownerId, "MANAGER"))
                        .param("includeDeleted", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @DisplayName("다른 점주는 가게 수정 불가")
    @Test
    void updateStore_fail_no_permission() throws Exception {
        UUID storeId = insertStoreWithRelations(
                ownerId,
                "사장가게",
                regionCode1,
                "주소",
                List.of(categoryId1)
        );

        String requestBody = """
                {
                  "name": "침입수정",
                  "regionCode": "%s",
                  "regionDetail": "침입주소",
                  "categoryIds": ["%s"]
                }
                """.formatted(regionCode2, categoryId2);

        mockMvc.perform(patch("/api/v1/stores/{storeId}", storeId)
                        .with(auth(anotherOwnerId, "OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is4xxClientError());
    }

    private RequestPostProcessor auth(Long userId, String role) {
        AuthUser authUser = Mockito.mock(AuthUser.class);
        when(authUser.getUserId()).thenReturn(userId);
        when(authUser.getRole()).thenReturn(role);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        return SecurityMockMvcRequestPostProcessors.authentication(authToken);
    }

    private UUID insertStoreWithRelations(
            Long ownerId,
            String storeName,
            UUID regionCode,
            String addressDetail,
            List<UUID> categoryIds
    ) {
        UUID storeId = UUID.randomUUID();
        UUID storeRegionId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update("""
            insert into p_store
            (store_id, owner_id, name, total_rating_sum, review_count, version,
             created_at, updated_at, created_by, updated_by, deleted_at, deleted_by)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                storeId,
                ownerId,
                storeName,
                0,
                0,
                0L,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now),
                ownerId,
                ownerId,
                null,
                null
        );

        jdbcTemplate.update("""
            insert into p_store_region
            (region_id, store_id, region_code, detail,
             created_at, updated_at, created_by, updated_by, deleted_at, deleted_by)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                storeRegionId,
                storeId,
                regionCode,
                addressDetail,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now),
                ownerId,
                ownerId,
                null,
                null
        );

        for (UUID categoryId : categoryIds) {
            jdbcTemplate.update("""
                insert into p_store_category
                (store_category_id, store_id, category_id,
                 created_at, updated_at, created_by, updated_by, deleted_at, deleted_by)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                    UUID.randomUUID(),
                    storeId,
                    categoryId,
                    Timestamp.valueOf(now),
                    Timestamp.valueOf(now),
                    ownerId,
                    ownerId,
                    null,
                    null
            );
        }

        return storeId;
    }

    private void softDeleteStore(UUID storeId, Long deletedBy) {
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update("""
            update p_store
            set deleted_at = ?, deleted_by = ?, updated_at = ?, updated_by = ?
            where store_id = ?
            """,
                Timestamp.valueOf(now),
                deletedBy,
                Timestamp.valueOf(now),
                deletedBy,
                storeId
        );
    }

    private void seedUser(Long userId, String role) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update("""
            insert into p_user
            (user_id, username, nickname, email, password, role,
             created_at, created_by, updated_at, updated_by)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                userId,
                "user" + userId,
                "nickname" + userId,
                "user" + userId + "@test.com",
                "password",
                role,
                now,
                userId,
                now,
                userId
        );
    }

    private void seedRegionCode(UUID regionCode, String regionName) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update("""
            insert into p_region_code
            (region_code, region_name, is_active,
             created_at, created_by, updated_at, updated_by)
            values (?, ?, ?, ?, ?, ?, ?)
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
            insert into p_category
            (category_id, name,
             created_at, created_by, updated_at, updated_by)
            values (?, ?, ?, ?, ?, ?)
            """,
                categoryId,
                name,
                now,
                0L,
                now,
                0L
        );
    }
}
