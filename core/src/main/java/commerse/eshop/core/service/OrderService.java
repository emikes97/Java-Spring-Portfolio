package commerse.eshop.core.service;

import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderPaymentMethod;
import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    // == Place Order ==
    DTOOrderPlacedResponse placeOrder(UUID customerId, DTOOrderCustomerAddress dto);

    // == Cancel Order ==
    void cancel(UUID customerId, UUID orderId);

    // == View Order ==
    DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId);
}
