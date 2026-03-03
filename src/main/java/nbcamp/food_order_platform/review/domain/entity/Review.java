package nbcamp.food_order_platform.review.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import nbcamp.food_order_platform.global.common.BaseEntity;
import nbcamp.food_order_platform.order.domain.entity.Order;
import nbcamp.food_order_platform.user.domain.entity.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name="p_review")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "review_id", columnDefinition = "uuid")
    private UUID reviewId;

    //  교체 완료
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

//    추후 머지 되면 교체
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store store;
    @Column(name = "store_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID storeId;

    // 교체 완료
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "content", length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ReviewStatus status = ReviewStatus.VISIBLE;

    public void updateReview(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public void updateStatus(ReviewStatus status) {
        this.status = status;
    }


}