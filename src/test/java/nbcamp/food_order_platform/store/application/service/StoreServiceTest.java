package nbcamp.food_order_platform.store.application.service;

import nbcamp.food_order_platform.category.domain.entity.Category;
import nbcamp.food_order_platform.category.domain.respository.CategoryRepository;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import nbcamp.food_order_platform.regionCode.domain.entity.RegionCode;
import nbcamp.food_order_platform.regionCode.domain.repository.RegionCodeRepository;
import nbcamp.food_order_platform.store.application.dto.common.StoreResult;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresAdminPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.GetStoresPageQuery;
import nbcamp.food_order_platform.store.application.dto.request.UpdateStoreCommand;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresAdminPageResult;
import nbcamp.food_order_platform.store.application.dto.response.GetStoresPageResult;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.entity.StoreCategory;
import nbcamp.food_order_platform.store.domain.entity.StoreRegion;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private RegionCodeRepository regionCodeRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private StoreService storeService;

    @DisplayName("가게 단건 조회 성공")
    @Test
    void getStore_success() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        StoreRegion storeRegion = mock(StoreRegion.class);
        StoreCategory storeCategory = mock(StoreCategory.class);
        StoreResult fakeResult = mock(StoreResult.class);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(store.getStoreRegion()).thenReturn(storeRegion);
        when(store.getStoreCategories()).thenReturn(List.of(storeCategory));

        try (MockedStatic<StoreResult> mocked = Mockito.mockStatic(StoreResult.class)) {
            mocked.when(() -> StoreResult.from(store, storeRegion, List.of(storeCategory)))
                    .thenReturn(fakeResult);

            // when
            StoreResult result = storeService.getStore(storeId);

            // then
            assertThat(result).isSameAs(fakeResult);
            verify(storeRepository).findById(storeId);
            verify(store).getStoreRegion();
            verify(store).getStoreCategories();
        }
    }

    @DisplayName("가게 단건 조회 시 가게가 없으면 NOT_EXISTED_STORE 예외가 발생한다")
    @Test
    void getStore_fails_when_store_not_found() {
        // given
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> storeService.getStore(storeId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_STORE);
                });
    }

    @DisplayName("가게 목록 조회 성공")
    @Test
    void getStores_success() {
        // given
        GetStoresPageQuery query = mock(GetStoresPageQuery.class);
        Pageable pageable = PageRequest.of(0, 10);

        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        StoreRegion region1 = mock(StoreRegion.class);
        StoreRegion region2 = mock(StoreRegion.class);
        StoreCategory category1 = mock(StoreCategory.class);
        StoreCategory category2 = mock(StoreCategory.class);

        StoreResult result1 = mock(StoreResult.class);
        StoreResult result2 = mock(StoreResult.class);
        GetStoresPageResult pageResult = mock(GetStoresPageResult.class);

        when(storeRepository.searchStores(
                query.getRegionCode(),
                query.getCategoryId(),
                query.getStoreName(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(store1, store2), pageable, 2));

        when(store1.getStoreRegion()).thenReturn(region1);
        when(store1.getStoreCategories()).thenReturn(List.of(category1));
        when(store2.getStoreRegion()).thenReturn(region2);
        when(store2.getStoreCategories()).thenReturn(List.of(category2));

        try (MockedStatic<StoreResult> storeResultMock = Mockito.mockStatic(StoreResult.class);
             MockedStatic<GetStoresPageResult> pageResultMock = Mockito.mockStatic(GetStoresPageResult.class)) {

            storeResultMock.when(() -> StoreResult.from(store1, region1, List.of(category1))).thenReturn(result1);
            storeResultMock.when(() -> StoreResult.from(store2, region2, List.of(category2))).thenReturn(result2);

            pageResultMock.when(() -> GetStoresPageResult.from(any(Page.class))).thenReturn(pageResult);

            // when
            GetStoresPageResult result = storeService.getStores(query, pageable);

            // then
            assertThat(result).isSameAs(pageResult);
            verify(storeRepository).searchStores(
                    query.getRegionCode(),
                    query.getCategoryId(),
                    query.getStoreName(),
                    pageable
            );
            pageResultMock.verify(() -> GetStoresPageResult.from(any(Page.class)), times(1));
        }
    }

    @DisplayName("가게 수정 성공 - 점주명, 가게명, 지역, 상세주소, 카테고리 변경")
    @Test
    void updateStore_success() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID newRegionCodeId = UUID.randomUUID();
        UUID currentCategoryId = UUID.randomUUID();
        UUID removedCategoryId = UUID.randomUUID();
        UUID addedCategoryId = UUID.randomUUID();

        UpdateStoreCommand command = mock(UpdateStoreCommand.class);
        when(command.getStoreId()).thenReturn(storeId);
        when(command.getOwnerId()).thenReturn(99L);
        when(command.getName()).thenReturn("new store");
        when(command.getRegionCode()).thenReturn(newRegionCodeId);
        when(command.getRegionDetail()).thenReturn("new detail");
        when(command.getCategoryIds()).thenReturn(List.of(currentCategoryId, addedCategoryId));

        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreRegion storeRegion = mock(StoreRegion.class);
        RegionCode currentRegionCode = mock(RegionCode.class);
        RegionCode newRegionCode = mock(RegionCode.class);

        when(store.getStoreRegion()).thenReturn(storeRegion);
        when(storeRegion.getRegionCode()).thenReturn(currentRegionCode);
        when(storeRegion.getDetail()).thenReturn("old detail");

        when(regionCodeRepository.findById(newRegionCodeId)).thenReturn(Optional.of(newRegionCode));

        Category currentCategory = mock(Category.class);
        Category removedCategory = mock(Category.class);
        Category addedCategory = mock(Category.class);

        when(currentCategory.getId()).thenReturn(currentCategoryId);
        when(removedCategory.getId()).thenReturn(removedCategoryId);
        when(addedCategory.getId()).thenReturn(addedCategoryId);

        StoreCategory currentStoreCategory = mock(StoreCategory.class);
        StoreCategory removedStoreCategory = mock(StoreCategory.class);
        when(currentStoreCategory.getCategory()).thenReturn(currentCategory);
        when(removedStoreCategory.getCategory()).thenReturn(removedCategory);

        when(store.getStoreCategories()).thenReturn(List.of(currentStoreCategory, removedStoreCategory));
        when(categoryRepository.findAllById(List.of(currentCategoryId, addedCategoryId)))
                .thenReturn(List.of(currentCategory, addedCategory));

        StoreResult fakeResult = mock(StoreResult.class);

        try (MockedStatic<StoreResult> mocked = Mockito.mockStatic(StoreResult.class)) {
            mocked.when(() -> StoreResult.from(eq(store), any(), any())).thenReturn(fakeResult);

            // when
            StoreResult result = storeService.updateStore(userId, command);

            // then
            assertThat(result).isSameAs(fakeResult);

            verify(userRepository).findById(userId);
            verify(storeRepository).findById(storeId);

            verify(store).changeOwner(99L);
            verify(store).changeName("new store");
            verify(store).changeRegion(newRegionCode, "new detail");

            verify(store).removeCategory(removedCategory, userId);
            verify(store, never()).removeCategory(eq(currentCategory), anyLong());

            verify(store).addCategory(currentCategory);
            verify(store).addCategory(addedCategory);
        }
    }

    @DisplayName("가게 수정 시 유저가 없으면 NOT_EXISTED_USER 예외가 발생한다")
    @Test
    void updateStore_fails_when_user_not_found() {
        // given
        Long userId = 1L;
        UpdateStoreCommand command = mock(UpdateStoreCommand.class);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> storeService.updateStore(userId, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_USER);
                });

        verifyNoInteractions(storeRepository, regionCodeRepository, categoryRepository);
    }

    @DisplayName("가게 수정 시 가게가 없으면 NOT_EXISTED_STORE 예외가 발생한다")
    @Test
    void updateStore_fails_when_store_not_found() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();

        UpdateStoreCommand command = mock(UpdateStoreCommand.class);
        when(command.getStoreId()).thenReturn(storeId);

        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> storeService.updateStore(userId, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_STORE);
                });
    }

    @DisplayName("가게 수정 시 존재하지 않는 지역이면 NOT_EXISTED_STORE_REGION 예외가 발생한다")
    @Test
    void updateStore_fails_when_region_not_found() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID regionCodeId = UUID.randomUUID();

        UpdateStoreCommand command = mock(UpdateStoreCommand.class);
        when(command.getStoreId()).thenReturn(storeId);
        when(command.getRegionCode()).thenReturn(regionCodeId);

        User user = mock(User.class);
        Store store = mock(Store.class);
        StoreRegion storeRegion = mock(StoreRegion.class);
        RegionCode currentRegionCode = mock(RegionCode.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(store.getStoreRegion()).thenReturn(storeRegion);
        when(storeRegion.getRegionCode()).thenReturn(currentRegionCode);
        when(storeRegion.getDetail()).thenReturn("old detail");

        when(regionCodeRepository.findById(regionCodeId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> storeService.updateStore(userId, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_STORE_REGION);
                });
    }

    @DisplayName("가게 수정 시 존재하지 않는 카테고리가 포함되면 NOT_EXISTED_STORE_CATEGORY 예외가 발생한다")
    @Test
    void updateStore_fails_when_category_not_found() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        UpdateStoreCommand command = mock(UpdateStoreCommand.class);
        when(command.getStoreId()).thenReturn(storeId);
        when(command.getOwnerId()).thenReturn(null);
        when(command.getName()).thenReturn(null);
        when(command.getRegionCode()).thenReturn(null);
        when(command.getRegionDetail()).thenReturn(null);
        when(command.getCategoryIds()).thenReturn(List.of(categoryId1, categoryId2));

        User user = mock(User.class);
        Store store = mock(Store.class);
        StoreRegion storeRegion = mock(StoreRegion.class);
        RegionCode currentRegionCode = mock(RegionCode.class);
        Category onlyOneCategory = mock(Category.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(store.getStoreRegion()).thenReturn(storeRegion);
        when(storeRegion.getRegionCode()).thenReturn(currentRegionCode);
        when(storeRegion.getDetail()).thenReturn("detail");

        when(categoryRepository.findAllById(List.of(categoryId1, categoryId2)))
                .thenReturn(List.of(onlyOneCategory));

        // when / then
        assertThatThrownBy(() -> storeService.updateStore(userId, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_STORE_CATEGORY);
                });
    }

    @DisplayName("가게 삭제 성공")
    @Test
    void deleteStore_success() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();

        User user = mock(User.class);
        Store store = mock(Store.class);
        StoreRegion storeRegion = mock(StoreRegion.class);
        StoreCategory storeCategory = mock(StoreCategory.class);
        StoreResult fakeResult = mock(StoreResult.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(store.getStoreRegion()).thenReturn(storeRegion);
        when(store.getStoreCategories()).thenReturn(List.of(storeCategory));

        try (MockedStatic<StoreResult> mocked = Mockito.mockStatic(StoreResult.class)) {
            mocked.when(() -> StoreResult.from(store, storeRegion, List.of(storeCategory)))
                    .thenReturn(fakeResult);

            // when
            StoreResult result = storeService.deleteStore(userId, storeId);

            // then
            assertThat(result).isSameAs(fakeResult);
            verify(store).softDelete(userId);
        }
    }

    @DisplayName("가게 삭제 시 유저가 없으면 NOT_EXISTED_USER 예외가 발생한다")
    @Test
    void deleteStore_fails_when_user_not_found() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> storeService.deleteStore(userId, storeId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_USER);
                });

        verifyNoInteractions(storeRepository);
    }

    @DisplayName("관리자 목록 조회 성공 - 삭제 제외")
    @Test
    void getAdminStores_success_without_deleted() {
        // given
        GetStoresAdminPageQuery query = mock(GetStoresAdminPageQuery.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(query.getIncludeDeleted()).thenReturn(false);

        Store store = mock(Store.class);
        StoreRegion storeRegion = mock(StoreRegion.class);
        StoreCategory storeCategory = mock(StoreCategory.class);
        StoreResult storeResult = mock(StoreResult.class);
        GetStoresAdminPageResult adminPageResult = mock(GetStoresAdminPageResult.class);

        when(storeRepository.searchAdminStores(
                query.getRegionCode(),
                query.getCategoryId(),
                query.getStoreName(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(store), pageable, 1));

        when(store.getStoreRegion()).thenReturn(storeRegion);
        when(store.getStoreCategories()).thenReturn(List.of(storeCategory));

        try (MockedStatic<StoreResult> storeResultMock = Mockito.mockStatic(StoreResult.class);
             MockedStatic<GetStoresAdminPageResult> pageResultMock = Mockito.mockStatic(GetStoresAdminPageResult.class)) {

            storeResultMock.when(() -> StoreResult.from(store, storeRegion, List.of(storeCategory)))
                    .thenReturn(storeResult);
            pageResultMock.when(() -> GetStoresAdminPageResult.from(any(Page.class)))
                    .thenReturn(adminPageResult);

            // when
            GetStoresAdminPageResult result = storeService.getAdminStores(query, pageable);

            // then
            assertThat(result).isSameAs(adminPageResult);
            verify(storeRepository).searchAdminStores(
                    query.getRegionCode(),
                    query.getCategoryId(),
                    query.getStoreName(),
                    pageable
            );
            verify(storeRepository, never()).searchAdminStoreIdsIncludingDeleted(any(), any(), any(), any());
            verify(storeRepository, never()).findAllByIdInIncludingDeleted(any());
        }
    }

    @DisplayName("관리자 목록 조회 성공 - 삭제 포함 시 삭제된 데이터도 결과에 포함된다")
    @Test
    void getAdminStores_success_with_deleted_includes_deleted_data() {
        // given
        GetStoresAdminPageQuery query = mock(GetStoresAdminPageQuery.class);
        Pageable pageable = PageRequest.of(0, 10);

        UUID deletedStoreId = UUID.randomUUID();
        UUID activeStoreId = UUID.randomUUID();

        when(query.getIncludeDeleted()).thenReturn(true);

        Page<UUID> idPage = new PageImpl<>(List.of(deletedStoreId, activeStoreId), pageable, 2);

        Store deletedStore = mock(Store.class);
        Store activeStore = mock(Store.class);

        StoreRegion deletedRegion = mock(StoreRegion.class);
        StoreRegion activeRegion = mock(StoreRegion.class);

        StoreCategory deletedCategory = mock(StoreCategory.class);
        StoreCategory activeCategory = mock(StoreCategory.class);

        StoreResult deletedResult = mock(StoreResult.class);
        StoreResult activeResult = mock(StoreResult.class);

        when(storeRepository.searchAdminStoreIdsIncludingDeleted(
                query.getRegionCode(),
                query.getCategoryId(),
                query.getStoreName(),
                pageable
        )).thenReturn(idPage);

        when(storeRepository.findAllByIdInIncludingDeleted(List.of(deletedStoreId, activeStoreId)))
                .thenReturn(List.of(deletedStore, activeStore));

        when(deletedStore.getId()).thenReturn(deletedStoreId);
        when(activeStore.getId()).thenReturn(activeStoreId);

        when(deletedStore.getStoreRegion()).thenReturn(deletedRegion);
        when(deletedStore.getStoreCategories()).thenReturn(List.of(deletedCategory));

        when(activeStore.getStoreRegion()).thenReturn(activeRegion);
        when(activeStore.getStoreCategories()).thenReturn(List.of(activeCategory));

        try (MockedStatic<StoreResult> storeResultMock = Mockito.mockStatic(StoreResult.class);
             MockedStatic<GetStoresAdminPageResult> pageResultMock = Mockito.mockStatic(GetStoresAdminPageResult.class)) {

            storeResultMock.when(() -> StoreResult.from(deletedStore, deletedRegion, List.of(deletedCategory)))
                    .thenReturn(deletedResult);
            storeResultMock.when(() -> StoreResult.from(activeStore, activeRegion, List.of(activeCategory)))
                    .thenReturn(activeResult);

            pageResultMock.when(() -> GetStoresAdminPageResult.from(Mockito.any(Page.class)))
                    .thenAnswer(invocation -> {
                        Page<StoreResult> page = invocation.getArgument(0);

                        assertThat(page.getContent()).hasSize(2);
                        assertThat(page.getContent().get(0)).isSameAs(deletedResult);
                        assertThat(page.getContent().get(1)).isSameAs(activeResult);
                        assertThat(page.getTotalElements()).isEqualTo(2);

                        return mock(GetStoresAdminPageResult.class);
                    });

            // when
            storeService.getAdminStores(query, pageable);

            // then
            verify(storeRepository).searchAdminStoreIdsIncludingDeleted(
                    query.getRegionCode(),
                    query.getCategoryId(),
                    query.getStoreName(),
                    pageable
            );
            verify(storeRepository).findAllByIdInIncludingDeleted(List.of(deletedStoreId, activeStoreId));
        }
    }

    @DisplayName("관리자 목록 조회 - 삭제 포함 시 조회된 ID가 비어있으면 빈 페이지를 반환한다")
    @Test
    void getAdminStores_returns_empty_page_when_no_ids() {
        // given
        GetStoresAdminPageQuery query = mock(GetStoresAdminPageQuery.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(query.getIncludeDeleted()).thenReturn(true);
        when(storeRepository.searchAdminStoreIdsIncludingDeleted(
                query.getRegionCode(),
                query.getCategoryId(),
                query.getStoreName(),
                pageable
        )).thenReturn(Page.empty(pageable));

        GetStoresAdminPageResult finalResult = mock(GetStoresAdminPageResult.class);

        try (MockedStatic<GetStoresAdminPageResult> pageResultMock = Mockito.mockStatic(GetStoresAdminPageResult.class)) {
            pageResultMock.when(() -> GetStoresAdminPageResult.from(any(Page.class))).thenReturn(finalResult);

            // when
            GetStoresAdminPageResult result = storeService.getAdminStores(query, pageable);

            // then
            assertThat(result).isSameAs(finalResult);
            verify(storeRepository, never()).findAllByIdInIncludingDeleted(any());
        }
    }
}