package nbcamp.food_order_platform.user.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.user.application.dto.*;
import nbcamp.food_order_platform.user.domain.entity.Address;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.AddressRepository;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final AddressService addressService;
    private final AddressRepository addressRepository;

    @Transactional
    public void signup(SignUpUserCommand command){

        validateUsername(command.userReqDto().getUsername());
        validateEmail(command.userReqDto().getEmail());
        validatePassword(command.userReqDto().getPassword());

        String encodedPassword = passwordEncoder.encode(command.userReqDto().getPassword());

        User user = User.builder()
                .username(command.userReqDto().getUsername())
                .email(command.userReqDto().getEmail())
                .password(encodedPassword)
                .nickname(command.userReqDto().getNickname())
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        CreateAddCommand addCommand = new CreateAddCommand(
                command.addReqDto().placeName(),
                command.addReqDto().roadName(),
                command.addReqDto().detailName());

        addressService.createAddress(user, addCommand);
    }

    private void validateUsername(String username){
        if(username.length() < 4 || username.length() > 10 ){
            throw new BusinessException(ErrorCode.AUTHORIZATION);
            //ErrorCode INVALID_ID_LENGTH추가
        }
        if(!username.matches("^[a-z0-9]{4,10}$")){
            throw new BusinessException(ErrorCode.AUTHORIZATION);
            //ErrorCode INVALID_ID_PATTERN추가
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
            //ErrorCode INVALID_PASSWORD_LENGTH추가
        }

        if(!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$")){
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_PATTERN);
            //ErrorCode INVALID_PASSWORD_PATTERN추가
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
        List<Address> addresses = addressRepository.findAllByUser_UserId(userId);

        for(Address address : addresses){
            address.softDelete(userId);
        }
        user.softDelete(userId);
    }

    public GetMyInfoResult getInfo(Long userId) {
        User user = userRepository.findAllByUserId(userId);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_EXISTED_USER);
        }
        List<Address> addresses = addressRepository.findAllByUser_UserIdAndDeletedAtIsNull(userId);

        List<AddressInfo> addressInfos = addresses.stream()
                .map(address -> new AddressInfo(
                        address.getPlaceName(),
                        address.getRoadName(),
                        address.getDetailName()
                ))
                .toList();

        return new GetMyInfoResult(
                user.getNickname(),
                user.getEmail(),
                user.getRole().name(),
                addressInfos
        );
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
