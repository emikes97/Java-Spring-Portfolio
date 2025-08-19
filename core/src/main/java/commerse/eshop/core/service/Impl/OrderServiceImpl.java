package commerse.eshop.core.service.Impl;

import commerse.eshop.core.service.OrderService;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class OrderServiceImpl implements OrderService {

    @Override
    public DTOOrderPlacedResponse placeOrderFromCart(UUID customerId, DTOAddCustomerAddress addressDto, DTOAddPaymentMethod paymentDto) {

        
        return null;
    }

    @Override
    public DTOOrderPlacedResponse placeOrderFromCart(UUID customerId) {
        return null;
    }

    @Override
    public void cancel(UUID customerId, long orderId) {

    }

    @Override
    public DTOOrderDetailsResponse viewOrder(UUID customerId, long orderId) {
        return null;
    }

    @Override
    public Page<DTOOrderDetailsResponse> viewAllOrders(UUID customerId, Pageable pageable) {
        return null;
    }
}
