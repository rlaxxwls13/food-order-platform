package nbcamp.food_order_platform.ai.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdateAiDescriptionCommand {
    private UUID aiLogId;
    private String responseText;
}
