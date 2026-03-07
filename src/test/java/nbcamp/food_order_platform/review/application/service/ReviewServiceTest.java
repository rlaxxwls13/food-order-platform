package nbcamp.food_order_platform.review.application.service;

import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.order.domain.entity.OrderStatus;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import nbcamp.food_order_platform.review.application.dto.*; // 변경된 CQRS DTO들
import nbcamp.food_order_platform.review.domain.entity.Review;
import nbcamp.food_order_platform.review.domain.entity.ReviewStatus;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
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

        // 테스트용 가게
        testStore = mock(Store.class);
        given(testStore.getId()).willReturn(storeId);

        // 테스트용 주문
        testOrder = mock(Order.class);
        given(testOrder.getCreatedAt()).willReturn(LocalDateTime.now());
        given(testOrder.getUser()).willReturn(testUser);
        given(testOrder.getOrderStatus()).willReturn(OrderStatus.COMPLETED);
        given(testOrder.getStore()).willReturn(storeId);

        // 테스트용 리뷰
        testReview = mock(Review.class);
        given(testReview.getReviewId()).willReturn(reviewId);
        given(testReview.getOrder()).willReturn(testOrder);
        given(testReview.getStore()).willReturn(testStore);
        given(testReview.getUser()).willReturn(testUser);
        given(testReview.getNickname()).willReturn("테스트유저");
        given(testReview.getRating()).willReturn(5); // 기존 별점 5점
        given(testReview.getContent()).willReturn("맛있어요");
        given(testReview.getStatus()).willReturn(ReviewStatus.VISIBLE);
    }

    // 1. 리뷰 작성 테스트
    @Nested
    @DisplayName("리뷰 작성")
    class CreateReview {

        @Test
        @DisplayName("성공 - 정상적으로 리뷰 작성, 스토어 통계가 갱신")
        void createReview_success() {
            // given
            CreateReviewCommand command = CreateReviewCommand.builder()
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
            CreateReviewResult result = reviewService.createReview(command);

            // then
            assertThat(result.getReviewId()).isEqualTo(reviewId);
            verify(reviewRepository, times(1)).save(any(Review.class));
            // 가게의 별점이 추가되었는지 검증!
            verify(testStore, times(1)).addNewRating(5);
        }
    }

    // 2-1. 리뷰 수정 테스트
    @Nested
    @DisplayName("리뷰 수정 (CUSTOMER)")
    class UpdateReview {

        @Test
        @DisplayName("성공 - 본인 리뷰를 수정하면 스토어 통계가 변경된다")
        void updateReview_success() {
            // given
            UpdateReviewCommand command = UpdateReviewCommand.builder()
                    .reviewId(reviewId)
                    .userId(1L)
                    .rating(4) // 5점에서 4점으로 수정
                    .content("수정된 내용")
                    .build();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));
            given(testReview.getUpdatedAt()).willReturn(LocalDateTime.now());

            // when
            UpdateReviewResult result = reviewService.updateReview(command);

            // then
            verify(testReview, times(1)).updateReview(4, "수정된 내용");
            // 스토어 통계 검증: 기존 5점 빼고 4점 더했는지 확인!
            verify(testStore, times(1)).removeRating(5);
            verify(testStore, times(1)).addNewRating(4);
            assertThat(result).isNotNull();
        }
    }

    // 2-2. 리뷰 상태 변경 테스트
    @Nested
    @DisplayName("리뷰 상태 변경 (MANAGER/MASTER)")
    class ChangeReviewStatus {

        @Test
        @DisplayName("성공 - MANAGER가 리뷰를 HIDDEN으로 변경")
        void changeStatus_success() {
            // given
            UpdateReviewStatusCommand command = UpdateReviewStatusCommand.builder()
                    .reviewId(reviewId)
                    .userId(2L) // Manager ID
                    .status(ReviewStatus.HIDDEN)
                    .build();

            given(userRepository.findById(2L)).willReturn(Optional.of(managerUser));
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));
            given(testReview.getUpdatedAt()).willReturn(LocalDateTime.now());

            // when
            UpdateReviewResult result = reviewService.changeReviewStatus(command);

            // then
            verify(testReview, times(1)).updateStatus(ReviewStatus.HIDDEN);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 상태 변경 시도")
        void changeStatus_noPermission() {
            // given
            UpdateReviewStatusCommand command = UpdateReviewStatusCommand.builder()
                    .reviewId(reviewId)
                    .userId(1L) // Customer ID
                    .status(ReviewStatus.HIDDEN)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.changeReviewStatus(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("권한이 필요합니다");
        }
    }

    // 3. 리뷰 삭제 테스트
    @Nested
    @DisplayName("리뷰 삭제")
    class DeleteReview {

        @Test
        @DisplayName("성공 - 본인 리뷰를 삭제하면 스토어 통계가 감소")
        void deleteReview_success() {
            // given
            DeleteReviewCommand command = DeleteReviewCommand.builder()
                    .reviewId(reviewId)
                    .userId(1L)
                    .build();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(testReview));

            // when
            reviewService.deleteReview(command);

            // then
            verify(testReview, times(1)).softDelete(1L);
            // 스토어 통계 검증: 삭제된 리뷰 별점(5)이 제거되었는지 확인!
            verify(testStore, times(1)).removeRating(5);
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
            visibleReview = mock(Review.class);
            given(visibleReview.getReviewId()).willReturn(UUID.randomUUID());
            given(visibleReview.getNickname()).willReturn("유저1");
            given(visibleReview.getStatus()).willReturn(ReviewStatus.VISIBLE);
            given(visibleReview.getStore()).willReturn(testStore);

            hiddenReview = mock(Review.class);
            given(hiddenReview.getReviewId()).willReturn(UUID.randomUUID());
            given(hiddenReview.getNickname()).willReturn("유저2");
            given(hiddenReview.getStatus()).willReturn(ReviewStatus.HIDDEN);
            given(hiddenReview.getStore()).willReturn(testStore);
        }

        @Test
        @DisplayName("성공 - CUSTOMER가 가게 리뷰 조회 시 VISIBLE만 보임")
        void getReviewsByStore_customer() {
            // given
            given(reviewRepository.findAllByStoreId(storeId))
                    .willReturn(List.of(visibleReview, hiddenReview));

            // when
            List<GetReviewCustomerResult> result = reviewService.getReviewsByStoreForCustomer(storeId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNickname()).isEqualTo("유저1");
        }

        @Test
        @DisplayName("성공 - MANAGER가 가게 리뷰 조회 시 전체 보암")
        void getReviewsByStore_manager() {
            // given
            GetReviewManagerQuery query = GetReviewManagerQuery.builder()
                    .storeId(storeId)
                    .userId(2L)
                    .build();

            given(userRepository.findById(2L)).willReturn(Optional.of(managerUser));
            given(reviewRepository.findAllByStoreId(storeId))
                    .willReturn(List.of(visibleReview, hiddenReview));

            // when
            List<GetReviewManagerResult> result = reviewService.getReviewsByStoreForManager(query);

            // then
            assertThat(result).hasSize(2);
        }
    }
}