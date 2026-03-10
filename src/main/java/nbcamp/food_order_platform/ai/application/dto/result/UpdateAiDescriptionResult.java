package nbcamp.food_order_platform.ai.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdateAiDescriptionResult {
    private UUID aiLogId;
    private String requestText;
    private String responseText;
    private LocalDateTime updatedAt;
}
