package nbcamp.food_order_platform.ai.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.ai.application.dto.command.CreateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.dto.result.GetAiDescriptionLogsResult;
import nbcamp.food_order_platform.ai.domain.entity.AiDescription;
import nbcamp.food_order_platform.ai.domain.repository.AiDescriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiLogService {
    private final AiDescriptionRepository aiDescriptionRepository;

    public void createAiLog(CreateAiDescriptionCommand aiDto) {
        AiDescription aiDescription = new AiDescription(
                aiDto.getProductId(),
                aiDto.getRequestText(),
                aiDto.getResponseText());
        aiDescriptionRepository.save(aiDescription);
    }

    public GetAiDescriptionLogsResult getAiDescriptionLogs(UUID productId, Pageable pageable) {
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
}