package nbcamp.food_order_platform.review.application.service;

import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.review.application.dto.CreateReviewDto;
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
import nbcamp.food_order_platform.review.presentation.dto.request.*;
import nbcamp.food_order_platform.review.presentation.dto.response.*;
import nbcamp.food_order_platform.store.domain.entity.Store;
import nbcamp.food_order_platform.store.domain.repository.StoreRepository;
import nbcamp.food_order_platform.user.domain.entity.Role;
import nbcamp.food_order_platform.user.domain.entity.User;
import nbcamp.food_order_platform.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private User managerUser;
    private Order testOrder;
    private Store testStore;
    private Review testReview;
    private UUID reviewId;
    private UUID orderId;
    private UUID storeId;

    @BeforeEach
    void setUp() {
        // 필요한 Id 랜덤 생성
        reviewId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        // 테스트용 CUSTOMER 유저
        testUser = mock(User.class);
        given(testUser.getUserId()).willReturn(1L);
        given(testUser.getNickname()).willReturn("테스트유저");
        given(testUser.getRole()).willReturn(Role.CUSTOMER);

        // 테스트용 MANAGER 유저
        managerUser = mock(User.class);
        given(managerUser.getUserId()).willReturn(2L);
        given(managerUser.getRole()).willReturn(Role.MANAGER);

        // 테스트용 가게 설정
        testStore = mock(Store.class);

        // 테스트용 주문 설정 (여기저기 흩어진 걸 하나로 합침!)
        testOrder = mock(Order.class);

        // ‼️‼️‼️‼️
        // Order에서 userId 타입을 Long으로 변경해야합니다
        // given(testOrder.getUserId()).willReturn(1L); // 본인 주문 검증 통과용 (testUser의 ID와 동일하게 1L 반환)
        given(testOrder.getOrderStatus()).willReturn(OrderStatus.COMPLETED); // 상태 검증 통과용
        given(testOrder.getStore()).willReturn(storeId); // ReviewService의 order.getStore() 호출 시 UUID 반환용
        given(testOrder.getUser()).willReturn(testUser);

        // 테스트용 리뷰
        testReview = mock(Review.class);
        given(testReview.getReviewId()).willReturn(reviewId);
        given(testReview.getOrder()).willReturn(testOrder);
        given(testStore.getId()).willReturn(storeId); // 가게 객체가 자신의 ID를 갖게 함
        given(testReview.getStore()).willReturn(testStore); // 리뷰가 가게 객체를 반환하게 함
        given(testReview.getUser()).willReturn(testUser);
        given(testReview.getNickname()).willReturn("테스트유저");
        given(testReview.getRating()).willReturn(5);
        given(testReview.getContent()).willReturn("맛있어요");
        given(testReview.getStatus()).willReturn(ReviewStatus.VISIBLE);
    }


    // 1. 리뷰 작성 테스트
    @Nested
    @DisplayName("리뷰 작성")
    class CreateReview {

        @Test
        @DisplayName("성공 - 정상적으로 리뷰가 작성된다")
        void createReview_success() {
            // given
            CreateReviewDto dto = CreateReviewDto.builder()
                    .orderId(orderId)
                    .userId(1L)
                    .rating(5)
                    .content("맛있어요")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            given(storeRepository.findById(storeId)).willReturn(Optional.of(testStore));

            given(orderRepository.findById(orderId)).willReturn(Optional.of(testOrder));
            given(reviewRepository.existsByOrderOrderId(orderId)).willReturn(false);

            given(reviewRepository.save(any(Review.class))).willReturn(testReview);
            given(testReview.getCreatedAt()).willReturn(LocalDateTime.now());

            // when
            PostReviewResDto result = reviewService.createReview(dto);

            // then
            assertThat(result.getReviewId()).isEqualTo(reviewId);
            assertThat(result.getStoreId()).isEqualTo(storeId);
            assertThat(result.getNickname()).isEqualTo("테스트유저");
            assertThat(result.getRating()).isEqualTo(5);
            assertThat(result.getContent()).isEqualTo("맛있어요");
            assertThat(result.getStatus()).isEqualTo(ReviewStatus.VISIBLE);
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저")
        void createReview_userNotFound() {
            // given
            CreateReviewDto dto = CreateReviewDto.builder()
                    .orderId(orderId)
                    .userId(999L)
                    .rating(5)
                    .content("맛있어요")
                    .build();

            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 회원");

            verify(reviewRepository, never()).save(any());
        }
    }


    // 2-1. 리뷰 수정 테스트
    @Nested
    @DisplayName("리뷰 수정 (CUSTOMER)")
    class UpdateReview {

        @Test
        @DisplayName("성공 - 본인 리뷰를 수정한다")
        void updateReview_success() {
            // given
            PatchReviewReqDto dto = new PatchReviewReqDto(4, "수정된 내용");

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));
            given(testReview.getUpdatedAt()).willReturn(LocalDateTime.now());

            // when
            PatchReviewResDto result = reviewService.updateReview(reviewId, 1L, dto);

            // then
            verify(testReview, times(1)).updateReview(4, "수정된 내용");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰")
        void updateReview_reviewNotFound() {
            // given
            PatchReviewReqDto dto = new PatchReviewReqDto(4, "수정된 내용");
            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(reviewId, 1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 리뷰");
        }

        @Test
        @DisplayName("실패 - 본인 리뷰가 아닌 경우")
        void updateReview_notOwner() {
            // given
            PatchReviewReqDto dto = new PatchReviewReqDto(4, "수정된 내용");
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));

            // when & then (다른 유저 ID로 시도)
            assertThatThrownBy(() -> reviewService.updateReview(reviewId, 999L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("권한");
        }
    }


    // 2-2. 리뷰 상태 변경 테스트
    @Nested
    @DisplayName("리뷰 상태 변경 (MANAGER/MASTER)")
    class ChangeReviewStatus {

        @Test
        @DisplayName("성공 - MANAGER가 리뷰를 HIDDEN으로 변경한다")
        void changeStatus_success() {
            // given
            PatchReviewStatusReqDto dto = new PatchReviewStatusReqDto(ReviewStatus.HIDDEN);
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));
            given(testReview.getUpdatedAt()).willReturn(LocalDateTime.now());

            // when
            PatchReviewResDto result = reviewService.changeReviewStatus(reviewId, managerUser, dto);

            // then
            verify(testReview, times(1)).updateStatus(ReviewStatus.HIDDEN);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 상태 변경 시도")
        void changeStatus_noPermission() {
            // given
            PatchReviewStatusReqDto dto = new PatchReviewStatusReqDto(ReviewStatus.HIDDEN);
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.changeReviewStatus(reviewId, testUser, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("매니저 또는 마스터");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰")
        void changeStatus_reviewNotFound() {
            // given
            PatchReviewStatusReqDto dto = new PatchReviewStatusReqDto(ReviewStatus.HIDDEN);
            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.changeReviewStatus(reviewId, managerUser, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 리뷰");
        }
    }

    // 3. 리뷰 삭제 테스트
    @Nested
    @DisplayName("리뷰 삭제")
    class DeleteReview {

        @Test
        @DisplayName("성공 - 본인 리뷰를 삭제한다")
        void deleteReview_success() {
            // given
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));

            // when
            reviewService.deleteReview(reviewId, 1L);

            // then
            verify(testReview, times(1)).softDelete(1L); // delete()에서 softDelete()로
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰")
        void deleteReview_reviewNotFound() {
            // given
            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 리뷰");
        }

        @Test
        @DisplayName("실패 - 본인 리뷰가 아닌 경우")
        void deleteReview_notOwner() {
            // given
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("본인");
        }
    }

    // 4. 리뷰 조회 테스트
    @Nested
    @DisplayName("리뷰 조회")
    class GetReviews {

        private Review visibleReview;
        private Review hiddenReview;

        @BeforeEach
        void setUpReviews() {
            // 엔티티 객체 생성
            visibleReview = Review.builder()
                    .reviewId(UUID.randomUUID())
                    .order(testOrder)
                    .store(testStore)
                    .user(testUser)
                    .nickname("유저1")
                    .rating(5)
                    .content("맛있어요")
                    .status(ReviewStatus.VISIBLE)
                    .build();

            hiddenReview = Review.builder()
                    .reviewId(UUID.randomUUID())
                    .order(testOrder)
                    .store(testStore)
                    .user(managerUser)
                    .nickname("유저2")
                    .rating(3)
                    .content("별로에요")
                    .status(ReviewStatus.HIDDEN)
                    .build();
        }

        @Test
        @DisplayName("성공 - CUSTOMER가 가게 리뷰 조회 시 VISIBLE만 보인다")
        void getReviewsByStore_customer() {
            // given
            given(reviewRepository.findAllByStoreId(storeId))
                    .willReturn(List.of(visibleReview, hiddenReview));

            // when
            List<GetReviewCustomerResDto> result =
                    reviewService.getReviewsByStoreForCustomer(storeId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNickname()).isEqualTo("유저1");
        }

        @Test
        @DisplayName("성공 - MANAGER가 가게 리뷰 조회 시 전체 보인다")
        void getReviewsByStore_manager() {
            // given
            given(reviewRepository.findAllByStoreId(storeId))
                    .willReturn(List.of(visibleReview, hiddenReview));

            // when
            List<GetReviewManagerResDto> result =
                    reviewService.getReviewsByStoreForManager(storeId, managerUser);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 관리자 조회 시도")
        void getReviewsByStore_manager_noPermission() {
            // when & then
            assertThatThrownBy(() ->
                    reviewService.getReviewsByStoreForManager(storeId, testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("관리자 권한");
        }

        @Test
        @DisplayName("성공 - 유저별 리뷰 조회 시 VISIBLE만 보인다")
        void getReviewsByUser() {
            // given
            given(reviewRepository.findAllByUser_UserId(1L))
                    .willReturn(List.of(visibleReview, hiddenReview));

            // when
            List<GetReviewCustomerResDto> result =
                    reviewService.getReviewsByUser(1L);

            // then
            assertThat(result).hasSize(1);
        }
    }
}