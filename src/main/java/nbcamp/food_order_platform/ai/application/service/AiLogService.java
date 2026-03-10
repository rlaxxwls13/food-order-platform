package nbcamp.food_order_platform.ai.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.ai.application.dto.command.CreateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.dto.command.UpdateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.dto.result.DeleteAiDescriptionLogResult;
import nbcamp.food_order_platform.ai.application.dto.result.GetAiDescriptionLogsResult;
import nbcamp.food_order_platform.ai.application.dto.result.UpdateAiDescriptionResult;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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

    //생성 설명 수정
    @Transactional
    public UpdateAiDescriptionResult updateAiDescription(UpdateAiDescriptionCommand aiDto, Long userId, Role role) {
        AiDescription aiDescription = aiDescriptionRepository.findById(aiDto.getAiLogId())
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXISTED_AI_LOG));

        Product product = productRepository.findById(aiDescription.getProductId())
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        validateStorePermission(product.getStoreId(), userId, role);

        if (!Objects.equals(aiDescription.getResponseText(), aiDto.getResponseText())) {
            product.changeDescription(aiDto.getResponseText());
            aiDescription.changeDescription(aiDto.getResponseText());
        }
        return new UpdateAiDescriptionResult(
                aiDescription.getId(),
                aiDescription.getRequestText(),
                aiDescription.getResponseText(),
                aiDescription.getUpdatedAt()
        );
    }

    //로그 삭제
    @Transactional
    public DeleteAiDescriptionLogResult deleteAiDescription(UUID aiLogId, Long userId, Role role) {
        AiDescription aiDescription = aiDescriptionRepository.findById(aiLogId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXISTED_AI_LOG));

        Product product = productRepository.findById(aiDescription.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_PRODUCT));

        validateStorePermission(product.getStoreId(), userId, role);

        aiDescription.softDelete(userId);

        return new DeleteAiDescriptionLogResult(
                aiDescription.getId(),
                aiDescription.getDeletedAt(),
                aiDescription.getDeletedBy()
        );
    }

    //가게 권한 확인
    public void validateStorePermission(UUID storeId, Long userId, Role role){
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        if (role == Role.MANAGER || role == Role.MASTER)
            return;

        if (role == Role.OWNER && store.getOwnerId().equals(userId))
            return;

        throw new BusinessException(ErrorCode.NO_PERMISSION, "가게 권한이 없습니다.");
    }
}