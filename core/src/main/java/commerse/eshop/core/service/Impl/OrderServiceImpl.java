package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.*;
import commerse.eshop.core.model.entity.consts.EndpointsNameMethods;
import commerse.eshop.core.model.entity.enums.AuditMessage;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.model.entity.enums.OrderStatus;
import commerse.eshop.core.repository.*;
import commerse.eshop.core.service.AuditingService;
import commerse.eshop.core.service.OrderService;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerse.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderItemsResponse;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import commerse.eshop.core.web.mapper.OrderServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    // == Fields ==
    private final CustomerRepo customerRepo;
    private final CartItemRepo cartItemRepo;
    private final CartRepo cartRepo;
    private final OrderItemRepo orderItemRepo;
    private final OrderRepo orderRepo;
    private final CustomerAddrRepo customerAddrRepo;
    private final CustomerPaymentMethodRepo customerPaymentMethodRepo;
    private final AuditingService auditingService;
    private final OrderServiceMapper orderServiceMapper;

    // == Constructors ==
    @Autowired
    public OrderServiceImpl(CustomerRepo customerRepo, CartItemRepo cartItemRepo, CartRepo cartRepo, OrderItemRepo orderItemRepo,
                            OrderRepo orderRepo, CustomerAddrRepo customerAddrRepo, CustomerPaymentMethodRepo customerPaymentMethodRepo,
                            AuditingService auditingService, OrderServiceMapper orderServiceMapper){

        this.customerRepo = customerRepo;
        this.cartItemRepo = cartItemRepo;
        this.cartRepo = cartRepo;
        this.orderItemRepo = orderItemRepo;
        this.orderRepo = orderRepo;
        this.customerAddrRepo = customerAddrRepo;
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.auditingService = auditingService;
        this.orderServiceMapper = orderServiceMapper;
    }

    // == Public Methods ==
    @Transactional
    @Override
    public DTOOrderPlacedResponse placeOrder(UUID customerId, DTOOrderCustomerAddress addressDto){

        // Validation of customerId
        if(customerId == null){
            IllegalArgumentException illegal = new IllegalArgumentException("The identification key can't be empty");
            auditingService.log(null, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        // Get cart_id from customer
        final  Cart cart;
        try {
            cart = cartRepo.findByCustomerCustomerId(customerId).orElseThrow(() ->
                    new NoSuchElementException("[ERROR] No cart exists for this customer."));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        // Get customer reference
        final Customer customer;
        try {
            customer = customerRepo.findById(customerId).orElseThrow(
                    () -> new NoSuchElementException("Customer account couldn't be found with the provided identification key= " + customerId)
            );
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        // If no address is provided, go for the default. (Default in our case is the one that the customer will pick during
        // checkout. In case to simulate it for now, is the default from what has been chosen in the db.

        if(addressDto == null){
            final  CustomerAddress customerAddress;
            try {
                customerAddress = customerAddrRepo.findByCustomerCustomerIdAndIsDefaultTrue(customerId).orElseThrow(
                        () -> new NoSuchElementException("The customer = " + customerId + " doesn't have a default address and " +
                                "no address has been provided for the order"));
            } catch (NoSuchElementException e){
                auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, e.toString());
                throw e;
            }

            addressDto = new DTOOrderCustomerAddress(customerAddress.getCountry(), customerAddress.getStreet(),
                    customerAddress.getCity(), customerAddress.getPostalCode());
        }

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = cartItemRepo.sumCartTotalOutstanding(cart.getCartId());
        if(Objects.isNull(total_outstanding) || total_outstanding.compareTo(BigDecimal.ZERO) <= 0 ){
            IllegalStateException illegal = new IllegalStateException("The cart is empty");
            auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        // Initiate a new order
        Order order = new Order(customer, orderServiceMapper.toMap(addressDto), total_outstanding);

        // set status to pending
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);

        // Decrement quantity of cart_items
        try {
           int updated = cartItemRepo.updateProductStock(cart.getCartId());
           long expected = cartItemRepo.countDistinctCartProducts(cart.getCartId());
           if(updated != expected){
               /// Client error, The product was out-of-stock when order was placed.
               ResponseStatusException err = new ResponseStatusException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK");
               auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, "INSUFFICIENT_STOCK");
               throw err;
           }
        } catch (TransientDataAccessResourceException | CannotCreateTransactionException ex){
            auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.ERROR, ex.toString());
            throw ex;
        }

        // Save order and flush
        try {
            orderRepo.saveAndFlush(order);
            // Snapshot cart items to order_items
            orderItemRepo.snapShotFromCart(order.getOrderId(), cart.getCartId());
            orderItemRepo.clearCart(cart.getCartId());
            auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_PLACE_SUCCESS.getMessage());
            return orderServiceMapper.toDto(order);
        } catch (DataIntegrityViolationException dub){
            auditingService.log(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.ERROR, dub.toString());
            throw dub;
        }
    }

    @Transactional
    @Override
    public void cancel(UUID customerId, UUID orderId) {

        if (customerId == null || orderId == null) {
            IllegalArgumentException bad = new IllegalArgumentException("Missing customerId/orderId.");
            auditingService.log(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "MISSING_IDS");
            throw bad;
        }

        final Order order;

        try {
            order = orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow( () -> new NoSuchElementException("There is no order with the ID=" + orderId));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        if(order.getOrderStatus() != OrderStatus.PENDING_PAYMENT){
            IllegalStateException illegal = new IllegalStateException("INVALID_STATE" + order.getOrderStatus());
            auditingService.log(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        try {
            int expected = orderRepo.countProductRowsToBeUpdated(orderId);

            if (expected == 0) {
                auditingService.log(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "ORDER_ITEMS_EMPTY_OR_MISSING");
                throw new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_ITEMS_EMPTY_OR_MISSING");
            }

            int updated = orderRepo.restoreProductStockFromOrder(orderId);

            if (updated != expected){
                /// Client error, The product was out-of-stock when order was placed.
                ResponseStatusException err = new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_ITEMS_EMPTY_OR_MISSING");
                auditingService.log(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "ORDER_ITEMS_EMPTY_OR_MISSING");
                throw err;
            }
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepo.saveAndFlush(order);
            auditingService.log(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_CANCEL_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId) {

        final Order order;

        try {
            order = orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow(
                    () -> new NoSuchElementException("The Order doesn't exist")
            );
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.ORDER_VIEW, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        var itemDtos = orderItemRepo.getOrderItems(orderId).stream()
                .map(oi -> new DTOOrderItemsResponse(
                        oi.getOrderItemId(),     // use snapshot columns on order_item
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getPriceAt()
                ))
                .toList();

        auditingService.log(customerId, EndpointsNameMethods.ORDER_VIEW, AuditingStatus.SUCCESSFUL, "ORDER_VIEW_SUCCESS");
        return orderServiceMapper.toDtoDetails(order, itemDtos);
    }
}