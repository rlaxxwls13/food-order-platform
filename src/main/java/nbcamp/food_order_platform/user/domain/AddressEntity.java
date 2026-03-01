package nbcamp.food_order_platform.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "p_address")
@Getter
@NoArgsConstructor
public class AddressEntity {

    @Id
    @GeneratedValue
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "road_name")
    private String roadName;

    @Column(name = "detail_name")
    private String detailName;
}
