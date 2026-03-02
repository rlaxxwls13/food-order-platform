package nbcamp.food_order_platform.user.presentation.controller;

import jakarta.validation.Valid;
import nbcamp.food_order_platform.user.presentation.dto.SignupRequestDto;
import nbcamp.food_order_platform.user.application.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public String signup(@Valid @RequestBody SignupRequestDto requestDto){
        userService.signup(requestDto);
        return "회원가입 성공";
    }
}
