package commerse.eshop.core.service;

import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    // == Customer Places Order ==
    DTOOrderPlacedResponse placeOrderFromCart(UUID customerId);

    // == Cancel Order ==
    void cancel(UUID customerId, long orderId);

    // == View Order ==
    DTOOrderDetailsResponse viewOrder(UUID customerId, long orderId);

    // == View all orders ==
    Page<DTOOrderDetailsResponse> viewAllOrders(UUID customerId, Pageable pageable);
}
