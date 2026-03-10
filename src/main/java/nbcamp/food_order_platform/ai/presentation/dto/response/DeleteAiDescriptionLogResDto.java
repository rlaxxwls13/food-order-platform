package nbcamp.food_order_platform.ai.presentation.dto.response;

import lombok.Getter;
import nbcamp.food_order_platform.ai.application.dto.result.DeleteAiDescriptionLogResult;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeleteAiDescriptionLogResDto {
    private UUID aiLogId;
    private LocalDateTime deletedAt;
    private Long deletedBy;

    public DeleteAiDescriptionLogResDto(DeleteAiDescriptionLogResult result) {
        this.aiLogId = result.getAiLogId();
        this.deletedAt = result.getDeletedAt();
        this.deletedBy = result.getDeletedBy();
    }
}