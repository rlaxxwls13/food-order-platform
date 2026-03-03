package nbcamp.food_order_platform.user.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import nbcamp.food_order_platform.user.presentation.dto.SignupRequestDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequestDto requestDto){

        validateUsername(requestDto.getUsername());
        validateEmail(requestDto.getEmail());

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);
    }

    private void validateUsername(String username){
        if(userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("이미있는 사용자 입니다");
            //추후에 GlobalExceptionHandler ErrorCode로 변경
        }
    }

    private void validateEmail(String email){
        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("이미 있는 이메일입니다.");
            //추후에 GlobalExceptionHandler ErrorCode로 변경
        }
    }
}
