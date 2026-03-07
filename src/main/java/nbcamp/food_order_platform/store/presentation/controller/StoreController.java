package nbcamp.food_order_platform.store.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.store.application.dto.request.CreateStoreCommand;
import nbcamp.food_order_platform.store.application.dto.common.StoreResult;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresAdminPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.UpdateStoreCommand;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresAdminPageResult;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresPageResult;
import nbcamp.food_order_platform.store.application.service.StoreService;
import nbcamp.food_order_platform.store.presentation.dto.common.StoreResDto;
import nbcamp.food_order_platform.store.presentation.dto.request.PostStoreReqDto;
import nbcamp.food_order_platform.store.presentation.dto.request.UpdateStoreReqDto;
import nbcamp.food_order_platform.store.presentation.dto.response.GetStoresAdminPageResDto;
import nbcamp.food_order_platform.store.presentation.dto.response.GetStoresPageResDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResDto> createStore(
            @AuthenticationPrincipal Long userId,
            // @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PostStoreReqDto request
    ) {
        // 추후에 권한 검증 추가
        CreateStoreCommand createStoreCommand = CreateStoreCommand.from(
                request.getName(),
                request.getRegionCodeId(),
                request.getAddressDetail(),
                request.getCategoryIds()
        );
        StoreResult result = storeService.createStore(userId, createStoreCommand);
        StoreResDto response = StoreResDto.from(
                result.getStore(), result.getStoreRegion(), result.getStoreCategories()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResDto> getStore(
            // @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID storeId
    ){
        // 추후에 권한 검증 추가
        StoreResult result = storeService.getStore(storeId);
        StoreResDto response = StoreResDto.from(
                result.getStore(),
                result.getStoreRegion(),
                result.getStoreCategories()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<GetStoresPageResDto> getStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID regionCode,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String storeName
    ) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));

        GetStoresPageQuery query = GetStoresPageQuery.from(regionCode, categoryId, storeName);
        GetStoresPageResult result = storeService.getStores(query, pageable);
        GetStoresPageResDto response = GetStoresPageResDto.from(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<GetStoresAdminPageResDto> getAdminStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID regionCode,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted
    ) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));

        GetStoresAdminPageQuery query = GetStoresAdminPageQuery.from(
                regionCode, categoryId, storeName, includeDeleted
        );
        GetStoresAdminPageResult result = storeService.getAdminStores(query, pageable);
        GetStoresAdminPageResDto response = GetStoresAdminPageResDto.from(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<StoreResDto> updateStore(
            @AuthenticationPrincipal Long userId,
            // @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID storeId,
            @Valid @RequestBody UpdateStoreReqDto request
    ) {
        UpdateStoreCommand command = UpdateStoreCommand.from(
                storeId, request.getOwnerId(),
                request.getName(), request.getRegionCode(),
                request.getRegionDetail(), request.getCategoryIds()
        );
        StoreResult result = storeService.updateStore(userId, command);
        StoreResDto response = StoreResDto.from(
                result.getStore(),
                result.getStoreRegion(),
                result.getStoreCategories()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<StoreResDto> deleteStore(
            @AuthenticationPrincipal Long userId,
            // @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID storeId
    ) {
        StoreResult result = storeService.deleteStore(userId, storeId);
        StoreResDto response = StoreResDto.from(
                result.getStore(),
                result.getStoreRegion(),
                result.getStoreCategories()
        );

        return ResponseEntity.ok(response);
    }

    private int normalizeSize(int size) {
        if (size == 10 || size == 30 || size == 50) {
            return size;
        }
        return 10;
    }
}
