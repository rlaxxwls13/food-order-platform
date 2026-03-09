package nbcamp.food_order_platform.user.presentation.controller;

import jakarta.validation.Valid;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.user.application.dto.GetUserDetailResult;
import nbcamp.food_order_platform.user.application.dto.GetUsersResult;
import nbcamp.food_order_platform.user.application.dto.PatchRoleCommand;
import nbcamp.food_order_platform.user.application.service.UserService;
import nbcamp.food_order_platform.user.presentation.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public String signup(@Valid @RequestBody SignupRequestDto requestDto){
        userService.signup(requestDto);
        return "회원가입 성공";
    }

    @GetMapping("/me")
    public GetMyInfoResDto getMyInfo(@AuthenticationPrincipal AuthUser authUser){

        return new GetMyInfoResDto(
                authUser.getUserId(),
                authUser.getUsername(),
                authUser.getRole()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    public List<GetUsersResDto> getUsers(){
        List<GetUsersResult> getUsersResults = userService.getUsers();

        return getUsersResults.stream()
                .map(getUsersResult -> new GetUsersResDto(
                        getUsersResult.userId(),
                        getUsersResult.username()
                )).toList();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    public GetUserDetailResDto getUserDetail(@PathVariable Long userId){
        GetUserDetailResult getUserDetailResult = userService.getUser(userId);
        return new GetUserDetailResDto(
                getUserDetailResult.userId(),
                getUserDetailResult.username(),
                getUserDetailResult.email(),
                getUserDetailResult.role()
        );
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('MASTER')")
    public void updateUserRole(@PathVariable Long userId ,@RequestBody PatchRoleReqDto patchRoleReqDto){
        PatchRoleCommand patchRoleCommand = new PatchRoleCommand(
                userId, patchRoleReqDto.role());
        userService.updateUserRole(patchRoleCommand);
    }
}
