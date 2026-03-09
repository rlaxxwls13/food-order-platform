package nbcamp.food_order_platform.user.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.user.application.dto.*;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import nbcamp.food_order_platform.user.presentation.dto.SignupRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        validatePassword(requestDto.getPassword());

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
        if(username.length() < 4 || username.length() > 10 ){
            throw new BusinessException(ErrorCode.INVALID_ID_LENGTH);
        }
        if(username.matches("^[a-z0-9]{4,10}$")){
            throw new BusinessException(ErrorCode.INVALID_ID_PATTERN);
        }

        if(userRepository.existsByUsername(username)){
            throw new BusinessException(ErrorCode.DUPLICATED_USER_ID);
        }
    }

    private void validateEmail(String email){
        if(userRepository.existsByEmail(email)){
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private void validatePassword(String password){
        if(password.length() < 8 || password.length() > 20){
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_LENGTH);
        }

        if(!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$")){
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_PATTERN);
        }
    }

    public Page<GetUsersResult> getUsers(String username, Pageable pageable) {
        Page<User> users;
        if(username == null){
            users = userRepository.findAllByDeletedAtIsNull(pageable);
        }else{
            users = userRepository
                    .findByUsernameContainingAndDeletedAtIsNull(username, pageable);
        }
        return users
                .map(user -> new GetUsersResult(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRole().name()
                ));
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

    public GetMyInfoResult getInfo(Long userId) {
        User user = userRepository.findAllByUserId(userId);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_EXISTED_USER);
        }
        return new GetMyInfoResult(user.getNickname(), user.getEmail(), user.getRole().name());
    }
    @Transactional
    public void updateInfo(UpdateInfoCommand command) {

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        if(command.dto().nickname() != null){
            user.updateNickname(command.dto().nickname());
        }

        if(command.dto().password() != null){
            String encodedPassword = passwordEncoder.encode(command.dto().password());
            user.updatePassword(encodedPassword);
        }
    }
}
