package nbcamp.food_order_platform.store.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.category.domain.entity.Category;
import nbcamp.food_order_platform.category.domain.respository.CategoryRepository;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import nbcamp.food_order_platform.regionCode.domain.repository.RegionCodeRepository;
import nbcamp.food_order_platform.store.application.dto.request.CreateStoreCommand;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresAdminPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.UpdateStoreCommand;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresAdminPageResult;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresPageResult;
import nbcamp.food_order_platform.store.application.dto.common.StoreResult;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.entity.StoreCategory;
import nbcamp.food_order_platform.store.domain.entity.StoreRegion;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final RegionCodeRepository regionCodeRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public StoreResult createStore(Long userId, CreateStoreCommand dto) {
        // 1) 입력 데이터 유효성 검증 및 권한 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        RegionCode regionCode = regionCodeRepository.findById(dto.getRegionCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE_REGION));
        List<UUID> categoryIds = dto.getCategoryIds() == null ? List.of() : dto.getCategoryIds();
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if(categoryIds.size() != categories.size())
            throw new BusinessException(ErrorCode.NOT_EXISTED_STORE_CATEGORY);

        // 2) 가게 저장
        Store newStore = new Store(userId, dto.getName(), regionCode, dto.getAddressDetail(), categories);
        storeRepository.save(newStore);

        // 3) 가게 정보 추출
        StoreRegion storeRegion = newStore.getStoreRegion();
        List<StoreCategory> storeCategories = newStore.getStoreCategories();

        return StoreResult.from(newStore, storeRegion, storeCategories);
    }

    @Transactional(readOnly = true)
    public StoreResult getStore(UUID storeId) {
        // 1) 입력 데이터 유효성 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        // 2) 가게 정보 추출
        StoreRegion storeRegion = store.getStoreRegion();
        List<StoreCategory> storeCategories = store.getStoreCategories();

        return StoreResult.from(store, storeRegion, storeCategories);
    }

    @Transactional(readOnly = true)
    public GetStoresPageResult getStores(GetStoresPageQuery query, Pageable pageable) {
        Page<Store> storePage = storeRepository.searchStores(
                query.getRegionCode(),
                query.getCategoryId(),
                query.getStoreName(),
                pageable
        );

        Page<StoreResult> resultPage = storePage.map(store ->
                StoreResult.from(store, store.getStoreRegion(), store.getStoreCategories())
        );

        return GetStoresPageResult.from(resultPage);
    }

    @Transactional
    public StoreResult updateStore(Long userId, UpdateStoreCommand command) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        Store store = storeRepository.findById(command.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));

        if(command.getOwnerId() != null)
            store.changeOwner(command.getOwnerId());

        if(command.getName() != null)
            store.changeName(command.getName());

        RegionCode regionCode = store.getStoreRegion().getRegionCode();
        String regionDetail = store.getStoreRegion().getDetail();
        if(command.getRegionCode() != null){
            UUID regionCodeId = command.getRegionCode();
            regionCode = regionCodeRepository.findById(regionCodeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE_REGION));
        }
        if(command.getRegionDetail() != null && !command.getRegionDetail().isBlank())
            regionDetail = command.getRegionDetail();
        store.changeRegion(regionCode, regionDetail);

        if(command.getCategoryIds() != null) {
            List<UUID> newCategoryIds = command.getCategoryIds();
            List<Category> newCategories = categoryRepository.findAllById(newCategoryIds);
            if(newCategoryIds.size() != newCategories.size())
                throw new BusinessException(ErrorCode.NOT_EXISTED_STORE_CATEGORY);

            List<Category> currentCategories = store.getStoreCategories().stream()
                    .map(StoreCategory :: getCategory)
                    .toList();

            for(Category currentCategory : currentCategories) {
                boolean shouldRemain = newCategories.stream()
                        .anyMatch(newCategory -> newCategory.getId().equals(currentCategory.getId()));
                if(!shouldRemain)
                    store.removeCategory(currentCategory, userId);
            }

            for(Category newCategory : newCategories) {
                store.addCategory(newCategory);
            }
        }

        return StoreResult.from(store, store.getStoreRegion(), store.getStoreCategories());
    }

    @Transactional
    public StoreResult deleteStore(Long userId, UUID storeId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_USER));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTED_STORE));
        store.softDelete(userId);

        return StoreResult.from(store, store.getStoreRegion(), store.getStoreCategories());
    }

    @Transactional(readOnly = true)
    public GetStoresAdminPageResult getAdminStores(GetStoresAdminPageQuery query, Pageable pageable) {
        if(!query.getIncludeDeleted()) {
            Page<Store> storePage = storeRepository.searchAdminStores(
                    query.getRegionCode(),
                    query.getCategoryId(),
                    query.getStoreName(),
                    pageable
            );

            Page<StoreResult> resultPage = storePage.map(store ->
                    StoreResult.from(store, store.getStoreRegion(), store.getStoreCategories())
            );
            return GetStoresAdminPageResult.from(resultPage);
        }
        Page<UUID> idPage = storeRepository.searchAdminStoreIdsIncludingDeleted(
                query.getRegionCode(),
                query.getCategoryId(),
                query.getStoreName(),
                pageable
        );

        List<UUID> storeIds = idPage.getContent();
        if (storeIds.isEmpty()) {
            return GetStoresAdminPageResult.from(Page.empty(pageable));
        }

        List<Store> stores = storeRepository.findAllByIdInIncludingDeleted(storeIds);

        Map<UUID, Store> storeMap = stores.stream()
                .collect(Collectors.toMap(Store::getId, Function.identity()));

        List<StoreResult> ordered = storeIds.stream()
                .map(storeMap::get)
                .filter(Objects::nonNull)
                .map(store -> StoreResult.from(store, store.getStoreRegion(), store.getStoreCategories()))
                .toList();

        Page<StoreResult> resultPage = new PageImpl<>(ordered, pageable, idPage.getTotalElements());
        return GetStoresAdminPageResult.from(resultPage);
    }

}
