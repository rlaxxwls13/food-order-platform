package nbcamp.food_order_platform.auth.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.auth.application.dto.LoginAuthCommand;
import nbcamp.food_order_platform.auth.application.dto.LoginAuthResult;
import nbcamp.food_order_platform.auth.application.dto.ReissueCommand;
import nbcamp.food_order_platform.auth.application.dto.ReissueResult;
import nbcamp.food_order_platform.auth.domain.entity.RefreshToken;
import nbcamp.food_order_platform.auth.domain.repository.RefreshRepository;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.global.security.JwtUtil;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginAuthResult login(LoginAuthCommand loginAuthCommand){
        User user = getUserByUsername(loginAuthCommand.username());

        validatePassword(loginAuthCommand.password(), user.getPassword());

        String accessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getUserId(),
                user.getRole()
        );

        refreshRepository.deleteByUserId(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getUsername(),
                user.getUserId(),
                user.getRole()
        );

        refreshRepository.save(
                new RefreshToken(
                        user.getUserId(),
                        refreshToken,
                        LocalDateTime.now().plusDays(7)
                )
        );

        return new LoginAuthResult(accessToken, refreshToken);
    }

    private User getUserByUsername(String username){
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

    public ReissueResult reissue(ReissueCommand reissueCommand) {

        String refreshToken = reissueCommand.refreshToken();

        Claims refreshClaims;

        try {
            refreshClaims = jwtUtil.parseToken(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.AUTHORIZATION);
            //EXPIRED_TOKEN ErrorCode 추가
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken token = refreshRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if(token.isExpired()){
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
            //ErrorCode.EXPIRED_TOKEN 추가
        }

        String type = refreshClaims.get("type", String.class);

        if (!"refresh".equals(type)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = token.getUserId();

        String username = refreshClaims.getSubject();
        Role role = Role.valueOf(refreshClaims.get("role", String.class));

        String newAccessToken = jwtUtil.generateAccessToken(username,userId, role);

        return new ReissueResult(newAccessToken);
    }
}
