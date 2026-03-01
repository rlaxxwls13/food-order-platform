package nbcamp.food_order_platform.review.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name="p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review { // 추후 BaseEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "review_id", columnDefinition = "uuid")
    private UUID reviewId;

//    추후 머지 되면 교체
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", nullable = false)
//    private Order order;
    @Column(name = "order_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID orderId;

//    추후 머지 되면 교체
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store store;
    @Column(name = "store_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID storeId;

//    추후 머지 되면 교체
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
    @Column(name = "user_id", nullable = false)
    private Long userId;

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


}