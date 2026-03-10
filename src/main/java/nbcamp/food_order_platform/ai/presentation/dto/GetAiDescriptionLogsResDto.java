package nbcamp.food_order_platform.ai.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbcamp.food_order_platform.ai.application.dto.result.GetAiDescriptionLogsResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetAiDescriptionLogsResDto {
    private List<AiDescriptionLogsSummaryDto> content;
    private int page;
    private int size;
    private long totalElements;

    @Getter
    @AllArgsConstructor
    public static class AiDescriptionLogsSummaryDto {
        private UUID productId;
        private UUID aiLogId;
        private String requestText;
        private String responseText;
        private LocalDateTime createdAt;
    }

    public static GetAiDescriptionLogsResDto from(GetAiDescriptionLogsResult result) {
        List<AiDescriptionLogsSummaryDto> content = result.getLogs().stream()
                .map(log -> new AiDescriptionLogsSummaryDto(
                        log.getProductId(),
                        log.getAiLogId(),
                        log.getRequestText(),
                        log.getResponseText(),
                        log.getCreatedAt()))
                .toList();
        return new GetAiDescriptionLogsResDto(
                content,
                result.getPage(),
                result.getSize(),
                result.getTotalCount());
    }
}