package nbcamp.food_order_platform.user.presentation.controller;

import jakarta.validation.Valid;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.user.application.dto.*;
import nbcamp.food_order_platform.user.application.service.UserService;
import nbcamp.food_order_platform.user.presentation.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

        GetMyInfoResult result = userService.getInfo(authUser.getUserId());

        return new GetMyInfoResDto(result.nickname(), result.email(), result.role());
    }

    @PatchMapping("/me")
    public void updateInfo(@AuthenticationPrincipal AuthUser authUser,
                           @Valid @RequestBody PatchUserReqDto dto){

        UpdateInfoCommand command = new UpdateInfoCommand(authUser.getUserId(), dto);

        userService.updateInfo(command);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    public Page<GetUsersResDto> getUsers(
            @RequestParam(required = false) String username,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ){
        Page<GetUsersResult> getUsersResults = userService.getUsers(username, pageable);

        return getUsersResults
                .map(getUsersResult -> new GetUsersResDto(
                        getUsersResult.userId(),
                        getUsersResult.username(),
                        getUsersResult.role()
                ));
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

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal AuthUser authUser){

        userService.deleteUser(authUser.getUserId());
    }
}
