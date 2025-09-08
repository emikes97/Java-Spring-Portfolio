package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;

import java.util.UUID;

public interface OrderService {

    // == Place Order ==
    DTOOrderPlacedResponse placeOrder(UUID customerId, DTOOrderCustomerAddress dto);

    // == Cancel Order ==
    void cancel(UUID customerId, UUID orderId);

    // == View Order ==
    DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId);
}
