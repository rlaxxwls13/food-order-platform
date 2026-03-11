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



}
