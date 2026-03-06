package nbcamp.food_order_platform.order.application.service;

import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;




}
