package nbcamp.food_order_platform.product;

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
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID storeId;
    private Long ownerId;
    private Long anotherOwnerId;

    @BeforeEach
    void setUp() {
        ownerId = 100L;
        anotherOwnerId = 999L;
        storeId = UUID.randomUUID();

        insertStore(storeId, ownerId, "테스트 가게");
    }

    @DisplayName("상품 등록 성공")
    @Test
    void createProduct_success() throws Exception {
        String requestBody = """
                {
                  "storeId": "%s",
                  "name": "콜라",
                  "stockQuantity": 30,
                  "price": 2000,
                  "description": "시원한 콜라",
                  "useAi": false
                }
                """.formatted(storeId);

        mockMvc.perform(post("/api/v1/products")
                        .with(auth(ownerId, "OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.name").value("콜라"))
                .andExpect(jsonPath("$.stockQuantity").value(30))
                .andExpect(jsonPath("$.price").value(2000))
                .andExpect(jsonPath("$.description").value("시원한 콜라"))
                .andExpect(jsonPath("$.hidden").value(false));
    }

    @DisplayName("상품 단건 조회 성공")
    @Test
    void getProduct_success() throws Exception {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, storeId, "사이다", "청량음료", 10, 1500, false, null, null);

        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .with(auth(ownerId, "OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.name").value("사이다"))
                .andExpect(jsonPath("$.price").value(1500))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.description").value("청량음료"))
                .andExpect(jsonPath("$.hidden").value(false));
    }

    @DisplayName("상품 수정 성공")
    @Test
    void updateProduct_success() throws Exception {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, storeId, "콜라", "기존 설명", 10, 2000, false, null, null);

        String requestBody = """
                {
                  "name": "제로콜라",
                  "description": "수정된 설명",
                  "addStockQuantity": 5,
                  "setStockQuantity": null,
                  "price": 2500,
                  "useAi": false
                }
                """;

        mockMvc.perform(patch("/api/v1/products/{productId}", productId)
                        .with(auth(ownerId, "OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("제로콜라"))
                .andExpect(jsonPath("$.description").value("수정된 설명"))
                .andExpect(jsonPath("$.stockQuantity").value(15))
                .andExpect(jsonPath("$.price").value(2500));
    }

    @DisplayName("상품 숨김 처리 성공")
    @Test
    void updateProductHidden_success() throws Exception {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, storeId, "콜라", "설명", 10, 2000, false, null, null);

        String requestBody = """
                {
                  "hidden": true
                }
                """;

        mockMvc.perform(patch("/api/v1/products/{productId}/hidden", productId)
                        .with(auth(ownerId, "OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.hidden").value(true));
    }

    @DisplayName("상품 삭제 성공")
    @Test
    void deleteProduct_success() throws Exception {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, storeId, "콜라", "설명", 10, 2000, false, null, null);

        mockMvc.perform(delete("/api/v1/products/{productId}", productId)
                        .with(auth(ownerId, "OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.deletedAt").exists())
                .andExpect(jsonPath("$.deletedBy").value(ownerId));
    }

    @DisplayName("일반 상품 목록 조회 성공 - 삭제 상품 제외")
    @Test
    void getProducts_success_excludes_deleted() throws Exception {
        UUID visibleProductId = UUID.randomUUID();
        UUID deletedProductId = UUID.randomUUID();

        insertProduct(visibleProductId, storeId, "콜라", "설명", 10, 2000, false, null, null);
        insertProduct(deletedProductId, storeId, "삭제상품", "설명", 10, 3000, false, LocalDateTime.now(), ownerId);

        mockMvc.perform(get("/api/v1/products")
                        .with(auth(ownerId, "OWNER"))
                        .param("storeId", storeId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productId").value(visibleProductId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("콜라"));
    }

    @DisplayName("관리자 상품 목록 조회 성공 - 숨김, 삭제 포함")
    @Test
    void getAdminProducts_success_include_hidden_and_deleted() throws Exception {
        UUID activeProductId = UUID.randomUUID();
        UUID deletedProductId = UUID.randomUUID();
        UUID hiddenProductId = UUID.randomUUID();

        insertProduct(activeProductId, storeId, "정상상품", "설명", 10, 1000, false, null, null);
        insertProduct(deletedProductId, storeId, "삭제상품", "설명", 10, 2000, false, LocalDateTime.now(), ownerId);
        insertProduct(hiddenProductId, storeId, "숨김상품", "설명", 10, 3000, true, null, null);

        mockMvc.perform(get("/api/v1/admin/products")
                        .with(auth(ownerId, "OWNER"))
                        .param("storeId", storeId.toString())
                        .param("includeHidden", "true")
                        .param("includeDeleted", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @DisplayName("다른 가게 사장은 상품 수정 불가")
    @Test
    void updateProduct_fail_no_permission() throws Exception {
        UUID productId = UUID.randomUUID();
        insertProduct(productId, storeId, "콜라", "설명", 10, 2000, false, null, null);

        String requestBody = """
                {
                  "name": "제로콜라",
                  "description": "수정된 설명",
                  "addStockQuantity": 5,
                  "setStockQuantity": null,
                  "price": 2500,
                  "useAi": false
                }
                """;

        mockMvc.perform(patch("/api/v1/products/{productId}", productId)
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

    private void insertStore(UUID storeId, Long ownerId, String name) {
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update("""
            INSERT INTO p_store
            (store_id, owner_id, name, total_rating_sum, review_count, version,
             created_at, updated_at, created_by, updated_by, deleted_at, deleted_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                storeId,
                ownerId,
                name,
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
    }

    private void insertProduct(
            UUID productId,
            UUID storeId,
            String name,
            String description,
            int quantity,
            int price,
            boolean hidden,
            LocalDateTime deletedAt,
            Long deletedBy
    ) {
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update("""
            INSERT INTO p_product
            (product_id, store_id, name, description, quantity, price, is_hidden, version,
             created_at, updated_at, created_by, updated_by, deleted_at, deleted_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                productId,
                storeId,
                name,
                description,
                quantity,
                price,
                hidden,
                0L,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now),
                ownerId,
                ownerId,
                deletedAt == null ? null : Timestamp.valueOf(deletedAt),
                deletedBy
        );
    }
}