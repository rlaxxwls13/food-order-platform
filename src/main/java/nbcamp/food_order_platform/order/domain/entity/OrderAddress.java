package nbcamp.food_order_platform.order.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddress {
    private String placeName;
    private String roadName;
    private String detailName;
}
