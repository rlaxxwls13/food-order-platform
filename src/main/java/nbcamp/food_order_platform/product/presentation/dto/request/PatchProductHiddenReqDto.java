package nbcamp.food_order_platform.product.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PatchProductHiddenReqDto {
    @NotNull(message = "hidden은 필수입니다.")
    private Boolean hidden;
}
