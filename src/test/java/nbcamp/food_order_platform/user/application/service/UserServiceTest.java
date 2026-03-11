package nbcamp.food_order_platform.user.application.service;

import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.user.application.dto.SignUpUserCommand;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import nbcamp.food_order_platform.user.presentation.dto.SignupAddReqDto;
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

    @Mock
    private AddressService addressService;

    @InjectMocks
    private UserService userService;

    @Test
    void signup_success() {

        SignupAddReqDto addDto =
                new SignupAddReqDto(
                        "집",
                        "서울 강남구",
                        "101호"
                );

        SignupRequestDto dto =
                new SignupRequestDto(
                        "testuse4",
                        "test4@email.com",
                        "Password4!",
                        "nickname",
                        addDto
                );

        SignUpUserCommand command =
                new SignUpUserCommand(dto, addDto);

        given(userRepository.existsByUsername("testuse4"))
                .willReturn(false);

        given(userRepository.existsByEmail("test4@email.com"))
                .willReturn(false);

        given(passwordEncoder.encode(any()))
                .willReturn("encodedPassword");

        userService.signup(command);

        verify(userRepository).save(any(User.class));
        verify(addressService).createAddress(any(User.class), any());
    }

    @Test
    void signup_failure_ID_Redundancy() {

        SignupAddReqDto addDto =
                new SignupAddReqDto(
                        "집",
                        "서울 강남구",
                        "101호"
                );

        SignupRequestDto dto =
                new SignupRequestDto(
                        "testuse4",
                        "test4@email.com",
                        "Password4!",
                        "nickname",
                        addDto
                );

        SignUpUserCommand command =
                new SignUpUserCommand(dto, addDto);

        given(userRepository.existsByUsername("testuse4"))
                .willReturn(true);

        assertThrows(
                BusinessException.class,
                () -> userService.signup(command)
        );
    }

    @Test
    void signup_failure_email_redundancy() {

        SignupAddReqDto addDto =
                new SignupAddReqDto(
                        "집",
                        "서울 강남구",
                        "101호"
                );

        SignupRequestDto dto =
                new SignupRequestDto(
                        "testuse4",
                        "test4@email.com",
                        "Password4!",
                        "nickname",
                        addDto
                );

        SignUpUserCommand command =
                new SignUpUserCommand(dto, addDto);

        given(userRepository.existsByUsername("testuse4"))
                .willReturn(false);

        given(userRepository.existsByEmail("test4@email.com"))
                .willReturn(true);

        assertThrows(
                BusinessException.class,
                () -> userService.signup(command)
        );
    }
}