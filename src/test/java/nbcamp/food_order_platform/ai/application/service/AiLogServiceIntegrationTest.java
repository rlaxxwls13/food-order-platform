package nbcamp.food_order_platform.ai.application.service;

import jakarta.persistence.EntityManager;
import nbcamp.food_order_platform.ai.application.dto.command.CreateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.dto.command.UpdateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.dto.result.GetAiDescriptionLogsResult;
import nbcamp.food_order_platform.ai.domain.entity.AiDescription;
import nbcamp.food_order_platform.ai.domain.repository.AiDescriptionRepository;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.product.application.dto.command.CreateProductCommand;
import nbcamp.food_order_platform.product.application.dto.result.CreateProductResult;
import nbcamp.food_order_platform.product.application.service.ProductService;
import nbcamp.food_order_platform.user.domain.entity.Role;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({"prod", "local"})
@Transactional
class AiLogServiceIntegrationTest {

    @Autowired
    private AiLogService aiLogService;

    @Autowired
    private ProductService productService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager em;
    @Autowired
    private AiDescriptionRepository aiDescriptionRepository;

    @DisplayName("AI 로그 조회 성공 - OWNER가 본인 상품의 로그를 조회")
    @Test
    void getAiDescriptionLogs_success() {
        // given
        long ownerId = 3001L;
        seedUser(ownerId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, ownerId, "가게");

        UUID productId = createProduct(ownerId, storeId, "콜라", 10, 2000, "탄산음료");

        aiLogService.createAiLog(new CreateAiDescriptionCommand(
                productId,
                "원본 설명 1",
                "AI 생성 설명 1"
        ));
        aiLogService.createAiLog(new CreateAiDescriptionCommand(
                productId,
                "원본 설명 2",
                "AI 생성 설명 2"
        ));
        flushAndClear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        GetAiDescriptionLogsResult result =
                aiLogService.getAiDescriptionLogs(productId, pageable, ownerId, Role.OWNER);

        // then
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        assertThat(result.getLogs())
                .extracting(GetAiDescriptionLogsResult.AiDescriptionLogsSummary::getProductId)
                .containsOnly(productId);

        assertThat(result.getLogs())
                .extracting(GetAiDescriptionLogsResult.AiDescriptionLogsSummary::getRequestText)
                .containsExactlyInAnyOrder("원본 설명 1", "원본 설명 2");

        assertThat(result.getLogs())
                .extracting(GetAiDescriptionLogsResult.AiDescriptionLogsSummary::getResponseText)
                .containsExactlyInAnyOrder("AI 생성 설명 1", "AI 생성 설명 2");
    }

    @DisplayName("AI 로그 조회 실패 - 상품이 없을 경우 NOT_EXISTED_PRODUCT")
    @Test
    void getAiDescriptionLogs_fail_product_not_found() {
        // given
        long ownerId = 3002L;
        seedUser(ownerId);

        UUID notExistedProductId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // when / then
        assertThatThrownBy(() ->
                aiLogService.getAiDescriptionLogs(notExistedProductId, pageable, ownerId, Role.OWNER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_PRODUCT);
                });
    }

    @DisplayName("AI 로그 조회 실패 - OWNER가 본인 가게 상품이 아니면 NO_PERMISSION")
    @Test
    void getAiDescriptionLogs_fail_owner_has_no_permission() {
        // given
        long realOwnerId = 3003L;
        long otherOwnerId = 3004L;

        seedUser(realOwnerId);
        seedUser(otherOwnerId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, realOwnerId, "사장님가게");

        UUID productId = createProduct(realOwnerId, storeId, "사이다", 5, 1800, "음료");

        aiLogService.createAiLog(new CreateAiDescriptionCommand(
                productId,
                "원본 설명",
                "AI 생성 설명"
        ));
        flushAndClear();

        Pageable pageable = PageRequest.of(0, 10);

        // when / then
        assertThatThrownBy(() ->
                aiLogService.getAiDescriptionLogs(productId, pageable, otherOwnerId, Role.OWNER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                });
    }

    @DisplayName("AI 설명 수정 성공")
    @Test
    void updateAiDescription_success() {
        // given
        long ownerId = 3005L;
        seedUser(ownerId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, ownerId, "수정가게");

        UUID productId = createProduct(ownerId, storeId, "콜라", 10, 2000, "탄산음료");

        aiLogService.createAiLog(new CreateAiDescriptionCommand(
                productId,
                "원본 설명",
                "AI 설명"
        ));
        flushAndClear();

        Pageable pageable = PageRequest.of(0, 10);

        UUID aiLogId = aiLogService
                .getAiDescriptionLogs(productId, pageable, ownerId, Role.OWNER)
                .getLogs()
                .get(0)
                .getAiLogId();

        UpdateAiDescriptionCommand command =
                new UpdateAiDescriptionCommand(aiLogId, "수정된 AI 설명");

        // when
        aiLogService.updateAiDescription(command, ownerId, Role.OWNER);
        flushAndClear();

        // then
        GetAiDescriptionLogsResult result =
                aiLogService.getAiDescriptionLogs(productId, pageable, ownerId, Role.OWNER);

        assertThat(result.getLogs().get(0).getResponseText())
                .isEqualTo("수정된 AI 설명");
    }

    @DisplayName("AI 로그 삭제 성공")
    @Test
    void deleteAiDescription_success() {
        // given
        long ownerId = 3006L;
        seedUser(ownerId);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, ownerId, "삭제가게");

        UUID productId = createProduct(ownerId, storeId, "콜라", 10, 2000, "탄산음료");

        aiLogService.createAiLog(new CreateAiDescriptionCommand(
                productId,
                "원본 설명",
                "AI 설명"
        ));
        flushAndClear();

        Pageable pageable = PageRequest.of(0, 10);

        UUID aiLogId = aiLogService
                .getAiDescriptionLogs(productId, pageable, ownerId, Role.OWNER)
                .getLogs()
                .get(0)
                .getAiLogId();

        // when
        aiLogService.deleteAiDescription(aiLogId, ownerId, Role.OWNER);
        flushAndClear();

        // then
        AiDescription deletedLog =
                aiDescriptionRepository.findById(aiLogId).orElseThrow();

        assertThat(deletedLog.getDeletedAt()).isNotNull();
        assertThat(deletedLog.getDeletedBy()).isEqualTo(ownerId);
    }

    @DisplayName("AI 로그 삭제 실패 - 권한 없음")
    @Test
    void deleteAiDescription_fail_no_permission() {
        // given
        long realOwner = 3007L;
        long otherOwner = 3008L;

        seedUser(realOwner);
        seedUser(otherOwner);

        UUID storeId = UUID.randomUUID();
        seedStore(storeId, realOwner, "사장가게");

        UUID productId = createProduct(realOwner, storeId, "콜라", 10, 2000, "탄산");

        aiLogService.createAiLog(new CreateAiDescriptionCommand(
                productId,
                "원본 설명",
                "AI 설명"
        ));
        flushAndClear();

        Pageable pageable = PageRequest.of(0, 10);

        UUID aiLogId = aiLogService
                .getAiDescriptionLogs(productId, pageable, realOwner, Role.OWNER)
                .getLogs()
                .get(0)
                .getAiLogId();

        // when / then
        assertThatThrownBy(() ->
                aiLogService.deleteAiDescription(aiLogId, otherOwner, Role.OWNER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                });
    }

    private UUID createProduct(Long userId, UUID storeId, String name, int quantity, int price, String description) {
        CreateProductCommand command = new CreateProductCommand(
                storeId,
                name,
                quantity,
                price,
                description,
                false
        );

        CreateProductResult result = productService.createProduct(command, userId, Role.OWNER);
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