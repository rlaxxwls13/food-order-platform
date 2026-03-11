package nbcamp.food_order_platform.user.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.user.application.dto.CreateAddCommand;
import nbcamp.food_order_platform.user.application.dto.InsertAddCommand;
import nbcamp.food_order_platform.user.application.dto.ListAddResult;
import nbcamp.food_order_platform.user.domain.entity.Address;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.AddressRepository;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createAddress(User user, CreateAddCommand command){

        Address address = Address.create(
                user,
                command.placeName(),
                command.roadName(),
                command.detailName()
        );

        addressRepository.save(address);
    }

    @Transactional
    public void insertAddress(Long userId, InsertAddCommand command) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));

        Address address = Address.create(
                user,
                command.placeName(), command.roadName(), command.detailName()
        );

        addressRepository.save(address);
    }

    public List<ListAddResult> listAddress(Long userId) {

        List<Address> addresses =
                addressRepository.findAllByUser_UserIdAndDeletedAtIsNull(userId);

        return addresses.stream()
                .map(address -> new ListAddResult(
                        address.getAddressId(),
                        address.getPlaceName(),
                        address.getRoadName(),
                        address.getDetailName()
                ))
                .toList();
    }

    @Transactional
    public void deleteAddress(Long userId, UUID addressId){

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_ADDRESS));

        if(address.isDeleted()){
            throw new BusinessException(ErrorCode.ALREADY_DELETED_ADDRESS);
        }

        if(!address.getUser().getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        Long count = addressRepository.countByUser_UserIdAndDeletedAtIsNull(userId);

        if(count <= 1){
            throw new BusinessException(ErrorCode.ADDRESS_REQUIRED);
        }

        address.softDelete(userId);
    }
}
