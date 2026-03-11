package nbcamp.food_order_platform.user.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbcamp.food_order_platform.global.common.BaseEntity;

import java.util.UUID;

@Entity(name = "p_address")
@Getter
@NoArgsConstructor
public class Address extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "road_name")
    private String roadName;

    @Column(name = "detail_name")
    private String detailName;

    //생성 메서드 추가
    public static Address create(User user, String placeName, String roadName, String detailName){

        Address address = new Address();
        address.user = user;
        address.placeName = placeName;
        address.roadName = roadName;
        address.detailName = detailName;

        return address;
    }
}
