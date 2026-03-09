package nbcamp.food_order_platform.ai.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateAiDescriptionResult {
    private UUID aiLogId;
    private UUID productId;
    private String requestText;
    private String responseText;
}

