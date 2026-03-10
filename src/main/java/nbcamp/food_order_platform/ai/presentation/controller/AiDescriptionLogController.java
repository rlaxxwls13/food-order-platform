package nbcamp.food_order_platform.ai.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.ai.application.dto.result.DeleteAiDescriptionLogResult;
import nbcamp.food_order_platform.ai.application.dto.result.UpdateAiDescriptionResult;
import nbcamp.food_order_platform.ai.application.dto.command.UpdateAiDescriptionCommand;
import nbcamp.food_order_platform.ai.application.dto.result.GetAiDescriptionLogsResult;
import nbcamp.food_order_platform.ai.application.service.AiLogService;
import nbcamp.food_order_platform.ai.presentation.dto.request.PatchAiDescriptionLogReqDto;
import nbcamp.food_order_platform.ai.presentation.dto.response.DeleteAiDescriptionLogResDto;
import nbcamp.food_order_platform.ai.presentation.dto.response.GetAiDescriptionLogsResDto;
import nbcamp.food_order_platform.ai.presentation.dto.response.PatchAiDescriptionLogResDto;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.user.domain.entity.Role;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiDescriptionLogController {

    private final AiLogService aiLogService;

    //상품별 조회
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

    //로그 수정
    @PatchMapping("/{aiLogId}")
    public ResponseEntity<PatchAiDescriptionLogResDto> updateProduct(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID aiLogId,
            @RequestBody PatchAiDescriptionLogReqDto requestDto
        ){
        UpdateAiDescriptionCommand aiDto = new UpdateAiDescriptionCommand(
                aiLogId,
                requestDto.getDescription()
        );

        UpdateAiDescriptionResult result = aiLogService.updateAiDescription(aiDto, authUser.getUserId(), Role.valueOf(authUser.getRole()));
        return ResponseEntity.ok(new PatchAiDescriptionLogResDto(result));
    }

//    로그 삭제(master, manager만)
    @DeleteMapping("/admin/{aiLogId}")
    public ResponseEntity<DeleteAiDescriptionLogResDto> deleteAiDescription(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID aiLogId
    ){
        DeleteAiDescriptionLogResult result = aiLogService.deleteAiDescription(
                aiLogId,
                authUser.getUserId(),
                Role.valueOf(authUser.getRole())
        );
        return ResponseEntity.ok(new DeleteAiDescriptionLogResDto(result));
    }
}
