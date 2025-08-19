package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.*;
import commerse.eshop.core.model.entity.enums.OrderStatus;
import commerse.eshop.core.repository.*;
import commerse.eshop.core.service.OrderService;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderPaymentMethod;
import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    public DTOOrderPlacedResponse placeOrderFromCart(UUID customerId, DTOOrderCustomerAddress addressDto, DTOOrderPaymentMethod paymentDto) {

        // #0 Get cart_id from customer
        Cart cart = cartRepo.findByCustomerCustomerId(customerId).orElseThrow(() ->
                new NoSuchElementException("[ERROR] No cart exists for this customer."));

        Customer customer = customerRepo.getReferenceById(customerId);

        // Start

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = cartItemRepo.sumCartTotalOutstanding(cart.getCartId());
        if(Objects.isNull(total_outstanding) || total_outstanding.compareTo(BigDecimal.ZERO) <= 0 )
            throw new IllegalStateException("The cart is empty");

        // #2 Create new order
        Order order = new Order(customer, toMap(addressDto), total_outstanding, OffsetDateTime.now());

        // #3 set status to pending
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        // Mid
        // #4 Decrement quantity of cart_items
        cartItemRepo.updateProductStock(cart.getCartId());

        // #5 Save order and flush
        orderRepo.saveAndFlush(order);

        // #6 Snapshot cart items to order_items
        orderItemRepo.snapShotFromCart(order.getOrderId(), cart.getCartId());

        // Event for transaction -> TransactionService.

        return toDto(order);
    }

    @Transactional
    @Override
    public DTOOrderPlacedResponse placeOrderFromCart(UUID customerId) {

        // == take the required fields ==

        // # 0 Get Customer reference
        Customer customer = customerRepo.getReferenceById(customerId);

        // # 1 Get Cart
        Cart cart = cartRepo.findByCustomerCustomerId(customerId).orElseThrow(() ->
                new NoSuchElementException("[ERROR] No cart exists for this customer."));

        // # 2 Get default address

        CustomerAddress customerAddress = customerAddrRepo.findByCustomerCustomerIdAndIsDefaultTrue(customerId).orElseThrow(
                () -> new NoSuchElementException("There isn't any default address")
        );

        DTOOrderCustomerAddress dtoOrderCustomerAddress = new DTOOrderCustomerAddress(customerAddress.getCountry(), customerAddress.getStreet(),
                customerAddress.getCity(), customerAddress.getPostalCode());

        // # 3 Get default PaymentMethod

        CustomerPaymentMethod customerPaymentMethod = customerPaymentMethodRepo.findByCustomerAndIsDefaultTrue(customer).orElseThrow(
                () -> new NoSuchElementException("There isn't any default payment method")
        );

        // == Start ==

        // #4 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding  = cartItemRepo.sumCartTotalOutstanding(cart.getCartId());
        if(Objects.isNull(total_outstanding) || total_outstanding.compareTo(BigDecimal.ZERO) <= 0 )
            throw new IllegalStateException("The cart is empty");

        // #5 Create new order
        Order order = new Order(customer, toMap(dtoOrderCustomerAddress), total_outstanding, OffsetDateTime.now());

        // #6 Set status to pending
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        // == Mid ==

        // #7 Decrement quantity of cart_items
        cartItemRepo.updateProductStock(cart.getCartId());

        // #8 Save order and flush
        orderRepo.saveAndFlush(order);

        // #9 Snapshot cart items to order_items
        orderItemRepo.snapShotFromCart(order.getOrderId(), cart.getCartId());

        // Event for transaction -> TransactionService.

        return toDto(order);
    }

    @Transactional
    @Override
    public void cancel(UUID customerId, UUID orderId) {
        // todo once schema is refactored and async is in place.
        // todo this job should be delegated to an async method, no point to have the customer wait for it to complete.
    }

    @Override
    public DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId) {
        Order order = orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow(
                () -> new NoSuchElementException("The order you requested doesn't exist")
        );
        Object OrderItem = orderItemRepo.findAll();
        return null;
    }

    @Override
    public Page<DTOOrderDetailsResponse> viewAllOrders(UUID customerId, Pageable pageable) {
        return null;
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