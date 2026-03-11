package nbcamp.food_order_platform.user.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.user.application.dto.GetUserDetailResult;
import nbcamp.food_order_platform.user.application.dto.GetUsersResult;
import nbcamp.food_order_platform.user.application.dto.PatchRoleCommand;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import nbcamp.food_order_platform.user.presentation.dto.SignupRequestDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
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
            throw new BusinessException(ErrorCode.DUPLICATED_USER_ID);
        }
    }

    private void validateEmail(String email){
        if(userRepository.existsByEmail(email)){
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    public List<GetUsersResult> getUsers() {
        List<User> users = userRepository.findAllByDeletedAtIsNull();
        return users.stream()
                .map(user -> new GetUsersResult(
                        user.getUserId(),
                        user.getUsername()
                ))
                .toList();
    }

    public GetUserDetailResult getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        return new GetUserDetailResult(user.getUserId(), user.getUsername(), user.getRole().name() , user.getEmail());
    }

    public void updateUserRole(PatchRoleCommand patchRoleCommand) {
        User user = userRepository.findById(patchRoleCommand.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        Role role;
        try{
            role = Role.valueOf(patchRoleCommand.role());
        }catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_ROLE);
        }
        user.updateRole(role);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        if(user.isDeleted()){
            throw new BusinessException(ErrorCode.ALREADY_DELETED_USER);
        }

        user.softDelete(userId);
    }
}
