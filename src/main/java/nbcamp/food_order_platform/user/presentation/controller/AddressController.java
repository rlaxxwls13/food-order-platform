package nbcamp.food_order_platform.user.presentation.controller;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.global.security.AuthUser;
import nbcamp.food_order_platform.user.application.dto.InsertAddCommand;
import nbcamp.food_order_platform.user.application.dto.ListAddResult;
import nbcamp.food_order_platform.user.application.service.AddressService;
import nbcamp.food_order_platform.user.presentation.dto.CreateAddressReqDto;
import nbcamp.food_order_platform.user.presentation.dto.ListAddResDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public void insertAddress(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CreateAddressReqDto dto
    ){
        InsertAddCommand command = new InsertAddCommand(
                dto.placeName(), dto.roadName(), dto.detailName());
        addressService.insertAddress(authUser.getUserId(), command);
    }


}