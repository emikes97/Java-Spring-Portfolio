package commerse.eshop.core.service;

import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderPaymentMethod;
import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    // == Customer Places Order -- CheckOut ==
    DTOOrderPlacedResponse placeOrderFromCart(UUID customerId, DTOOrderCustomerAddress addressDto, DTOOrderPaymentMethod paymentDto);

    // == Customer Places Order -- CheckOut ==
    DTOOrderPlacedResponse placeOrderFromCart(UUID customerId); // Use defaults if not applicable reject the order.

    // == Cancel Order ==
    void cancel(UUID customerId, long orderId);

    // == View Order ==
    DTOOrderDetailsResponse viewOrder(UUID customerId, long orderId);

    // == View all orders ==
    Page<DTOOrderDetailsResponse> viewAllOrders(UUID customerId, Pageable pageable);
}
