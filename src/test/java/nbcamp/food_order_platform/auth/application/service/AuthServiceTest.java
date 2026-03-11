package nbcamp.food_order_platform.auth.application.service;

import nbcamp.food_order_platform.auth.application.dto.LoginAuthCommand;
import nbcamp.food_order_platform.auth.application.dto.LoginAuthResult;
import nbcamp.food_order_platform.auth.domain.repository.RefreshRepository;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshRepository refreshRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void 로그인_성공() {

        // given
        LoginAuthCommand command =
                new LoginAuthCommand("testuser", "Password1!");

        User user = User.builder()
                .username("testuser")
                .email("test@email.com")
                .password("encodedPassword")
                .nickname("nick")
                .role(Role.CUSTOMER)
                .build();

        // JPA @GeneratedValue 필드 테스트용 세팅
        ReflectionTestUtils.setField(user, "userId", 1L);

        given(userRepository.findByUsername("testuser"))
                .willReturn(Optional.of(user));

        given(passwordEncoder.matches("Password1!", "encodedPassword"))
                .willReturn(true);

        given(jwtUtil.generateAccessToken(anyString(), anyLong(), any()))
                .willReturn("access-token");

        given(jwtUtil.generateRefreshToken(anyString(), anyLong(), any()))
                .willReturn("refresh-token");

        // refresh token 관련 mock
        doNothing().when(refreshRepository).deleteByUserId(any());

        given(refreshRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        LoginAuthResult result = authService.login(command);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void 로그인_실패_아이디없음() {

        LoginAuthCommand command =
                new LoginAuthCommand("testuser", "Password1!");

        given(userRepository.findByUsername("testuser"))
                .willReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> authService.login(command)
        );
    }

    @Test
    void 로그인_실패_비밀번호불일치() {

        LoginAuthCommand command =
                new LoginAuthCommand("testuser", "Password1!");

        User user = User.builder()
                .username("testuser")
                .email("test@email.com")
                .password("encodedPassword")
                .nickname("nick")
                .role(Role.CUSTOMER)
                .build();

        ReflectionTestUtils.setField(user, "userId", 1L);

        given(userRepository.findByUsername("testuser"))
                .willReturn(Optional.of(user));

        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);

        assertThrows(
                BusinessException.class,
                () -> authService.login(command)
        );
    }

    @Test
    void 로그아웃_성공() {

        Long userId = 1L;

        authService.logout(userId);

        verify(refreshRepository).deleteByUserId(userId);
    }
}