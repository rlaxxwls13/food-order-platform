package nbcamp.food_order_platform.ai.application.service;

import nbcamp.food_order_platform.ai.application.dto.command.CreateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.dto.result.GetAiDescriptionLogsResult;
import nbcamp.food_order_platform.ai.domain.entity.AiDescription;
import nbcamp.food_order_platform.ai.domain.repository.AiDescriptionRepository;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.product.domain.entity.Product;
import nbcamp.food_order_platform.product.domain.repository.ProductRepository;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiLogServiceTest {

    @Mock
    private AiDescriptionRepository aiDescriptionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private AiLogService aiLogService;

    @DisplayName("AI 로그 생성 성공")
    @Test
    void createAiLog_success() {
        // given
        UUID productId = UUID.randomUUID();
        CreateAiDescriptionCommand command =
                new CreateAiDescriptionCommand(productId, "원본 설명", "AI 생성 설명");

        ArgumentCaptor<AiDescription> captor = ArgumentCaptor.forClass(AiDescription.class);

        // when
        aiLogService.createAiLog(command);

        // then
        verify(aiDescriptionRepository).save(captor.capture());

        AiDescription savedLog = captor.getValue();
        assertThat(savedLog.getProductId()).isEqualTo(productId);
        assertThat(savedLog.getRequestText()).isEqualTo("원본 설명");
        assertThat(savedLog.getResponseText()).isEqualTo("AI 생성 설명");
    }

    @DisplayName("AI 로그 조회 성공 - OWNER")
    @Test
    void getAiDescriptionLogs_success() {
        // given
        Long userId = 1L;
        UUID productId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Product product = mock(Product.class);
        Store store = mock(Store.class);
        AiDescription log1 = mock(AiDescription.class);
        AiDescription log2 = mock(AiDescription.class);

        LocalDateTime createdAt1 = LocalDateTime.now().minusDays(1);
        LocalDateTime createdAt2 = LocalDateTime.now();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getStoreId()).thenReturn(storeId);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(store.getOwnerId()).thenReturn(userId);

        when(aiDescriptionRepository.findByProductId(productId, pageable))
                .thenReturn(new PageImpl<>(List.of(log1, log2), pageable, 2));

        when(log1.getProductId()).thenReturn(productId);
        when(log1.getId()).thenReturn(UUID.randomUUID());
        when(log1.getRequestText()).thenReturn("원본 설명1");
        when(log1.getResponseText()).thenReturn("AI 설명1");
        when(log1.getCreatedAt()).thenReturn(createdAt1);

        when(log2.getProductId()).thenReturn(productId);
        when(log2.getId()).thenReturn(UUID.randomUUID());
        when(log2.getRequestText()).thenReturn("원본 설명2");
        when(log2.getResponseText()).thenReturn("AI 설명2");
        when(log2.getCreatedAt()).thenReturn(createdAt2);

        // when
        GetAiDescriptionLogsResult result =
                aiLogService.getAiDescriptionLogs(productId, pageable, userId, Role.OWNER);

        // then
        assertThat(result.getLogs()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        assertThat(result.getLogs().get(0).getProductId()).isEqualTo(productId);
        assertThat(result.getLogs().get(0).getRequestText()).isEqualTo("원본 설명1");
        assertThat(result.getLogs().get(0).getResponseText()).isEqualTo("AI 설명1");

        assertThat(result.getLogs().get(1).getRequestText()).isEqualTo("원본 설명2");
        assertThat(result.getLogs().get(1).getResponseText()).isEqualTo("AI 설명2");
    }

    @DisplayName("AI 로그 조회 시 상품이 없으면 NOT_EXISTED_PRODUCT 예외가 발생한다")
    @Test
    void getAiDescriptionLogs_fails_product_not_found() {
        // given
        Long userId = 1L;
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> aiLogService.getAiDescriptionLogs(productId, pageable, userId, Role.OWNER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_PRODUCT);
                });

        verifyNoInteractions(storeRepository);
        verify(aiDescriptionRepository, never()).findByProductId(any(), any());
    }

    @DisplayName("AI 로그 조회 시 OWNER가 본인 가게가 아니면 NO_PERMISSION 예외가 발생한다")
    @Test
    void getAiDescriptionLogs_fails_owner_has_no_permission() {
        // given
        Long userId = 1L;
        Long otherOwnerId = 2L;
        UUID productId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Product product = mock(Product.class);
        Store store = mock(Store.class);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getStoreId()).thenReturn(storeId);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(store.getOwnerId()).thenReturn(otherOwnerId);

        // when / then
        assertThatThrownBy(() -> aiLogService.getAiDescriptionLogs(productId, pageable, userId, Role.OWNER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NO_PERMISSION);
                });

        verify(aiDescriptionRepository, never()).findByProductId(any(), any());
    }
}
