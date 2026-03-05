package nbcamp.food_order_platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.auth.application.dto.LoginAuthCommand;
import nbcamp.food_order_platform.auth.application.dto.LoginAuthResult;
import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginAuthResult login(LoginAuthCommand loginAuthCommand){
        User user = findUserByUsername(loginAuthCommand.username());

        validatePassword(loginAuthCommand.password(), user.getPassword());

        String accessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getUserId(),
                user.getRole()
        );
        return new LoginAuthResult(accessToken);
    }

    private User findUserByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 아이디 입니다."));
                //추후에 GlobalExceptionHandler ErrorCode로 변경
    }

    private void validatePassword(String rawPassword, String encodePassword){
        if(!passwordEncoder.matches(rawPassword, encodePassword)) {
            throw new IllegalArgumentException("비밀번호가 일지하지 않습니다.");
            //추후에 GlobalExceptionHandler ErrorCode로 변경
        }
    }
}
