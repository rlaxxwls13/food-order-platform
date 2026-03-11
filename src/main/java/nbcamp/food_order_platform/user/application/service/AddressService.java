package nbcamp.food_order_platform.user.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.user.application.dto.CreateAddCommand;
import nbcamp.food_order_platform.user.domain.entity.Address;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

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
}
