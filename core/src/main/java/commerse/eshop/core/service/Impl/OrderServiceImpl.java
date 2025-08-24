package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.*;
import commerse.eshop.core.model.entity.enums.OrderStatus;
import commerse.eshop.core.repository.*;
import commerse.eshop.core.service.OrderService;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerRepo customerRepo;
    private final CartItemRepo cartItemRepo;
    private final CartRepo cartRepo;
    private final OrderItemRepo orderItemRepo;
    private final OrderRepo orderRepo;
    private final CustomerAddrRepo customerAddrRepo;
    private final CustomerPaymentMethodRepo customerPaymentMethodRepo;

    @Autowired
    public OrderServiceImpl(CustomerRepo customerRepo, CartItemRepo cartItemRepo, CartRepo cartRepo, OrderItemRepo orderItemRepo,
                            OrderRepo orderRepo, CustomerAddrRepo customerAddrRepo, CustomerPaymentMethodRepo customerPaymentMethodRepo){
        this.customerRepo = customerRepo;
        this.cartItemRepo = cartItemRepo;
        this.cartRepo = cartRepo;
        this.orderItemRepo = orderItemRepo;
        this.orderRepo = orderRepo;
        this.customerAddrRepo = customerAddrRepo;
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
    }

    @Transactional
    @Override
    public DTOOrderPlacedResponse placeOrder(UUID customerId, DTOOrderCustomerAddress addressDto){

        // Get cart_id from customer
        Cart cart = cartRepo.findByCustomerCustomerId(customerId).orElseThrow(() ->
                new NoSuchElementException("[ERROR] No cart exists for this customer."));

        // Get a customer Reference
        Customer customer = customerRepo.getReferenceById(customerId);

        // If no address is provided, go for the default. (Default in our case is the one that the customer will pick during
        // checkout. In case to simulate it for now, is the default from what has been chosen in the db.

        if(addressDto == null){

            CustomerAddress customerAddress = customerAddrRepo.findByCustomerCustomerIdAndIsDefaultTrue(customerId).orElseThrow(
                    () -> new NoSuchElementException("There isn't any default address"));

            addressDto = new DTOOrderCustomerAddress(customerAddress.getCountry(), customerAddress.getStreet(),
                    customerAddress.getCity(), customerAddress.getPostalCode());
        }

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = cartItemRepo.sumCartTotalOutstanding(cart.getCartId());
        if(Objects.isNull(total_outstanding) || total_outstanding.compareTo(BigDecimal.ZERO) <= 0 )
            throw new IllegalStateException("The cart is empty");

        // Initiate a new order
        Order order = new Order(customer, toMap(addressDto), total_outstanding);

        // set status to pending
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);

        // Decrement quantity of cart_items
        cartItemRepo.updateProductStock(cart.getCartId());

        // Save order and flush
        orderRepo.saveAndFlush(order);

        // Snapshot cart items to order_items
        orderItemRepo.snapShotFromCart(order.getOrderId(), cart.getCartId());
        orderItemRepo.clearCart(cart.getCartId());

        return toDto(order);
    }

    @Transactional
    @Override
    public void cancel(UUID customerId, UUID orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow( () -> new NoSuchElementException("There is no order with the ID=" + orderId));

        if(order.getOrderStatus() != OrderStatus.PENDING_PAYMENT && order.getOrderStatus() != OrderStatus.PAYMENT_FAILED){
            throw new IllegalStateException("Order can't be canceled as its state is:" + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepo.restoreProductStockFromOrder(orderId);

        orderRepo.save(order);
    }

    @Override
    public DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId) {
        Order order = orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow(
                () -> new NoSuchElementException("The Order doesn't exist")
        );

        List<OrderItem> orderItems = orderItemRepo.getOrderItems(orderId);

        return toDtoDetails(order, orderItems);
    }

    private Map<String,Object> toMap(DTOOrderCustomerAddress dto){
        Map<String, Object> map = new HashMap<>();
        map.put("country", dto.country());
        map.put("street", dto.street());
        map.put("city", dto.city());
        map.put("postalCode", dto.postalCode());
        return map;
    }

    private DTOOrderCustomerAddress toAdDto(Map<String, Object> addr){
        return new DTOOrderCustomerAddress(
                (String) addr.get("country"),
                (String) addr.get("street"),
                (String) addr.get("city"),
                (String) addr.get("postalCode"));
    }

    private DTOOrderPlacedResponse toDto(Order o){
        return new DTOOrderPlacedResponse(
                o.getOrderId(),
                o.getTotalOutstanding(),
                toAdDto(o.getAddressToSend()),
                o.getCreatedAt(),
                o.getCompletedAt()
        );}

    private DTOOrderDetailsResponse toDtoDetails(Order o, List<OrderItem> orderItems ){

        return new DTOOrderDetailsResponse(
                o.getOrderId(),
                o.getTotalOutstanding(),
                toAdDto(o.getAddressToSend()),
                orderItems,
                o.getCreatedAt(),
                o.getCompletedAt()
        );
    }
}