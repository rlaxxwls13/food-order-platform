package nbcamp.food_order_platform.ai.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateAiDescriptionCommand {
    private UUID productId;
    private String requestText;
    private String responseText;
}

