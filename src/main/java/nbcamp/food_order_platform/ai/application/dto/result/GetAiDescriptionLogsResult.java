package nbcamp.food_order_platform.ai.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetAiDescriptionLogsResult {
    private List<AiDescriptionLogsSummary> logs;
    private int page;
    private int size;
    private long totalCount;

    @Getter
    @AllArgsConstructor
    public static class AiDescriptionLogsSummary {
        private UUID productId;
        private UUID aiLogId;
        private String requestText;
        private String responseText;
        private LocalDateTime createdAt;
    }
}
