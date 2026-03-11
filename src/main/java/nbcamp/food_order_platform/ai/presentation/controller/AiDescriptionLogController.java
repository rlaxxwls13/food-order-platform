package nbcamp.food_order_platform.ai.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.ai.application.dto.result.GetAiDescriptionLogsResult;
import nbcamp.food_order_platform.ai.application.service.AiLogService;
import nbcamp.food_order_platform.ai.presentation.dto.GetAiDescriptionLogsResDto;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.user.domain.entity.Role;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiDescriptionLogController {

    private final AiLogService aiLogService;

    @GetMapping("/logs")
    public GetAiDescriptionLogsResDto getAiDescriptionLogs(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);

        GetAiDescriptionLogsResult result = aiLogService.getAiDescriptionLogs(productId, pageable, authUser.getUserId(), Role.valueOf(authUser.getRole()));

        return GetAiDescriptionLogsResDto.from(result);
    }
}
