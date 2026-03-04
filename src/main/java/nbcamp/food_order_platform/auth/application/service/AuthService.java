package nbcamp.food_order_platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.auth.presentation.dto.PostAuthReqDto;
import nbcamp.food_order_platform.auth.presentation.dto.PostAuthResDto;
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

    public PostAuthResDto login(PostAuthReqDto postAuthReqDto){
        User user = findUserByUsername(postAuthReqDto.getUsername());

        validatePassword(postAuthReqDto.getPassword(), user.getPassword());

        String accessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getUserId(),
                user.getRole()
        );
        return new PostAuthResDto(accessToken);
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
