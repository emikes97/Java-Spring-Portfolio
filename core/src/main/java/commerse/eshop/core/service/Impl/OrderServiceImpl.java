package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Cart;
import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.Order;
import commerse.eshop.core.model.entity.enums.OrderStatus;
import commerse.eshop.core.repository.CartItemRepo;
import commerse.eshop.core.repository.CartRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.repository.OrderItemRepo;
import commerse.eshop.core.service.OrderService;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderPaymentMethod;
import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerRepo customerRepo;
    private final CartItemRepo cartItemRepo;
    private final CartRepo cartRepo;
    private final OrderItemRepo orderItemRepo;

    @Autowired
    public OrderServiceImpl(CustomerRepo customerRepo, CartItemRepo cartItemRepo, CartRepo cartRepo, OrderItemRepo orderItemRepo){
        this.customerRepo = customerRepo;
        this.cartItemRepo = cartItemRepo;
        this.cartRepo = cartRepo;
        this.orderItemRepo = orderItemRepo;
    }

    @Transactional
    @Override
    public DTOOrderPlacedResponse placeOrderFromCart(UUID customerId, DTOOrderCustomerAddress addressDto, DTOOrderPaymentMethod paymentDto) {

        // #0 Get cart_id from customer
        Cart cart = cartRepo.findByCustomerCustomerId(customerId).orElseThrow(() ->
                new NoSuchElementException("[ERROR] No cart exists for this customer."));

        Customer customer = customerRepo.getReferenceById(customerId).;

        // Start

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = cartItemRepo.sumCartTotalOutstanding(cart.getCartId());
        if(total_outstanding.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException("The cart is empty");

        // #2 Create new order // Get the UUID for the new order.
        Order order = new Order(customer, toMap(addressDto), total_outstanding, OffsetDateTime.now());

        // #3 Save order and set status to pending
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        // #4 Decrement quantity of cart_items

        // Mid

        // Event + Order save.

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

    public Map<String,Object> toMap(DTOOrderCustomerAddress dto){
        Map<String, Object> map = new HashMap<>();
        map.put("country", dto.country());
        map.put("street", dto.street());
        map.put("city", dto.city());
        map.put("postalCode", dto.postalCode());
        return map;
    }
}
