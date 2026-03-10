package nbcamp.food_order_platform.ai.application.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiLogService {
    private final AiDescriptionRepository aiDescriptionRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    //ai로그 생성
    public void createAiLog(CreateAiDescriptionCommand aiDto) {
        AiDescription aiDescription = new AiDescription(
                aiDto.getProductId(),
                aiDto.getRequestText(),
                aiDto.getResponseText());
        aiDescriptionRepository.save(aiDescription);
    }

    //로그 조회
    public GetAiDescriptionLogsResult getAiDescriptionLogs(UUID productId, Pageable pageable, Long userId, Role role) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        validateStorePermission(product.getStoreId(), userId, role);

        Page<AiDescription> logPage = aiDescriptionRepository.findByProductId(productId, pageable);
        List<GetAiDescriptionLogsResult.AiDescriptionLogsSummary> logs = logPage.getContent().stream()
                .map(log -> new GetAiDescriptionLogsResult.AiDescriptionLogsSummary(
                        log.getProductId(),
                        log.getId(),
                        log.getRequestText(),
                        log.getResponseText(),
                        log.getCreatedAt()))
                .toList();

        return new GetAiDescriptionLogsResult(
                logs,
                logPage.getNumber(),
                logPage.getSize(),
                logPage.getTotalElements()
        );
    }

    public void validateStorePermission(UUID storeId, Long userId, Role role){ //가게 주인 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        if (role == Role.MANAGER || role == Role.MASTER)
            return;

        if (role == Role.OWNER && store.getOwnerId().equals(userId))
            return;

        throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
    }
}