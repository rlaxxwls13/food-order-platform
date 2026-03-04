package nbcamp.food_order_platform.auth.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.auth.application.service.AuthService;
import nbcamp.food_order_platform.auth.presentation.dto.PostAuthReqDto;
import nbcamp.food_order_platform.auth.presentation.dto.PostAuthResDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public PostAuthResDto login(@Valid @RequestBody PostAuthReqDto postAuthReqDto){
        return authService.login(postAuthReqDto);
    }

}
