package nbcamp.food_order_platform.ai.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.ai.application.dto.command.CreateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.domain.entity.AiDescription;
import nbcamp.food_order_platform.ai.domain.repository.AiDescriptionRepository;
import org.springframework.stereotype.Service;

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
}