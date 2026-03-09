package nbcamp.food_order_platform.store.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStoreReqDto {

    @NotBlank(message = "가게명은 필수입니다.")
    @Size(max = 255, message = "가게명은 255자 이하여야 합니다.")
    private String name;

    @NotNull(message = "regionCodeId는 필수입니다.")
    private UUID regionCodeId;

    @NotBlank(message = "addressDetail은 필수입니다.")
    private String addressDetail;

    private List<UUID> categoryIds;

}
