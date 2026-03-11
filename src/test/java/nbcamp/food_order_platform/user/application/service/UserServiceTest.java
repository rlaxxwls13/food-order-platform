package nbcamp.food_order_platform.user.application.service;

import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import nbcamp.food_order_platform.user.presentation.dto.SignupRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void signup_success() {

        SignupRequestDto dto =
                new SignupRequestDto(
                        "testuse1",
                        "test@email.com",
                        "Password1!",
                        "nickname"
                );

        given(userRepository.existsByUsername("testuse1"))
                .willReturn(false);

        given(userRepository.existsByEmail("test@email.com"))
                .willReturn(false);

        given(passwordEncoder.encode(any()))
                .willReturn("encodedPassword");

        userService.signup(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_failure_ID_Redundancy() {

        SignupRequestDto dto =
                new SignupRequestDto(
                        "testuse1",
                        "test@email.com",
                        "Password1!",
                        "nickname"
                );

        given(userRepository.existsByUsername("testuse1"))
                .willReturn(true);

        assertThrows(
                BusinessException.class,
                () -> userService.signup(dto)
        );
    }

    @Test
    void signUp_Failure_email_redundancy() {

        SignupRequestDto dto =
                new SignupRequestDto(
                        "testuse1",
                        "test@email.com",
                        "Password1!",
                        "nickname"
                );

        given(userRepository.existsByUsername("testuse1"))
                .willReturn(false);

        given(userRepository.existsByEmail("test@email.com"))
                .willReturn(true);

        assertThrows(
                BusinessException.class,
                () -> userService.signup(dto)
        );
    }
}