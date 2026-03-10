package nbcamp.food_order_platform.ai.presentation.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeleteAiDescriptionLogResDto {
    private UUID aiLogId;
    private LocalDateTime deletedAt;
    private Long deletedBy;

    public DeleteProductLogResDto(DeleteAiDescriptionLogResDto result) {
        this.aiLogId = result.getAiLogId();
        this.deletedAt = result.getDeletedAt();
        this.deletedBy = result.getDeletedBy();
    }
}
