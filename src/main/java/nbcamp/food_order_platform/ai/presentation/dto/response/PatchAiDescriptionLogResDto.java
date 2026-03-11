package nbcamp.food_order_platform.ai.presentation.dto.response;

import lombok.Getter;
import nbcamp.food_order_platform.ai.application.dto.result.UpdateAiDescriptionResult;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PatchAiDescriptionLogResDto {
    private UUID aiLogId;
    private String requestText;
    private String responseText;
    private LocalDateTime updatedAt;

    public PatchAiDescriptionLogResDto(UpdateAiDescriptionResult result) {
        this.aiLogId = result.getAiLogId();
        this.requestText = result.getRequestText();
        this.responseText = result.getResponseText();
        this.updatedAt = result.getUpdatedAt();
    }
}
