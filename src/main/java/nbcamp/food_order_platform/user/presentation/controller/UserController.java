package nbcamp.food_order_platform.user.presentation.controller;

import jakarta.validation.Valid;
import nbcamp.food_order_platform.user.application.service.UserService;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.presentation.dto.GetUserResDto;
import nbcamp.food_order_platform.user.presentation.dto.SignupRequestDto;
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
    public GetUserResDto information(@AuthenticationPrincipal User user){

        return new GetUserResDto(
                user.getUserId(),
                user.getUsername(),
                user.getRole()
        );
    }
}
