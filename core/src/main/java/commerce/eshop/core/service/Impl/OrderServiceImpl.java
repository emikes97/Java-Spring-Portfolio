package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.repository.*;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.enums.OrderStatus;
import commerce.eshop.core.service.OrderService;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderItemsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import commerce.eshop.core.web.mapper.OrderServiceMapper;
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
    private final CentralAudit centralAudit;
    private final OrderServiceMapper orderServiceMapper;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public OrderServiceImpl(CustomerRepo customerRepo, CartItemRepo cartItemRepo, CartRepo cartRepo, OrderItemRepo orderItemRepo,
                            OrderRepo orderRepo, CustomerAddrRepo customerAddrRepo, CustomerPaymentMethodRepo customerPaymentMethodRepo,
                            CentralAudit centralAudit, OrderServiceMapper orderServiceMapper, DomainLookupService domainLookupService){

        this.customerRepo = customerRepo;
        this.cartItemRepo = cartItemRepo;
        this.cartRepo = cartRepo;
        this.orderItemRepo = orderItemRepo;
        this.orderRepo = orderRepo;
        this.customerAddrRepo = customerAddrRepo;
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.centralAudit = centralAudit;
        this.orderServiceMapper = orderServiceMapper;
        this.domainLookupService = domainLookupService;
    }

    // == Public Methods ==
    @Transactional
    @Override
    public DTOOrderPlacedResponse placeOrder(UUID customerId, DTOOrderCustomerAddress addressDto){

        // Validation of customerId
        if(customerId == null){
            IllegalArgumentException illegal = new IllegalArgumentException("The identification key can't be empty");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, illegal.toString());
        }

        // Get cart_id from customer
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);

        // Get customer reference
        final Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);

        // If no address is provided, go for the default. (Default in our case is the one that the customer will pick during
        // checkout. In case to simulate it for now, is the default from what has been chosen in the db.

        if(addressDto == null){
            final CustomerAddress customerAddress = domainLookupService.getCustomerAddrOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);
            addressDto = new DTOOrderCustomerAddress(customerAddress.getCountry(), customerAddress.getStreet(),
                    customerAddress.getCity(), customerAddress.getPostalCode());
        }

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = cartItemRepo.sumCartTotalOutstanding(cart.getCartId());
        if(Objects.isNull(total_outstanding) || total_outstanding.compareTo(BigDecimal.ZERO) <= 0 ){
            IllegalStateException illegal = new IllegalStateException("The cart is empty");
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, illegal.toString());
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
               throw centralAudit.audit(err, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, "INSUFFICIENT_STOCK");
           }
        } catch (TransientDataAccessResourceException | CannotCreateTransactionException ex){
           throw centralAudit.audit(ex, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.ERROR, ex.toString());
        }

        // Save order and flush
        try {
            orderRepo.saveAndFlush(order);
            // Snapshot cart items to order_items
            orderItemRepo.snapShotFromCart(order.getOrderId(), cart.getCartId());
            orderItemRepo.clearCart(cart.getCartId());
            centralAudit.info(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_PLACE_SUCCESS.getMessage());
            return orderServiceMapper.toDto(order);
        } catch (DataIntegrityViolationException dub){
           throw centralAudit.audit(dub, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.ERROR, dub.toString());
        }
    }

    @Transactional
    @Override
    public void cancel(UUID customerId, UUID orderId) {

        if (customerId == null || orderId == null) {
            IllegalArgumentException bad = new IllegalArgumentException("Missing customerId/orderId.");
            throw centralAudit.audit(bad, customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "MISSING_IDS");
        }

        final Order order = domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_CANCEL);

        if(order.getOrderStatus() != OrderStatus.PENDING_PAYMENT){
            IllegalStateException illegal = new IllegalStateException("INVALID_STATE" + order.getOrderStatus());
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, illegal.toString());
        }

        try {
            int expected = orderRepo.countProductRowsToBeUpdated(orderId);

            if (expected == 0) {
                throw centralAudit.audit(new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_ITEMS_EMPTY_OR_MISSING"), customerId,
                        EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "ORDER_ITEMS_EMPTY_OR_MISSING");
            }

            int updated = orderRepo.restoreProductStockFromOrder(orderId);

            if (updated != expected){
                /// Client error, The product was out-of-stock when order was placed.
                ResponseStatusException err = new ResponseStatusException(HttpStatus.CONFLICT, "ORDER_ITEMS_EMPTY_OR_MISSING");
                throw centralAudit.audit(err, customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.WARNING, "ORDER_ITEMS_EMPTY_OR_MISSING");
            }
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepo.saveAndFlush(order);
            centralAudit.info(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_CANCEL_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId) {

        final Order order = domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_VIEW);

        var itemDtos = orderItemRepo.getOrderItems(orderId).stream()
                .map(oi -> new DTOOrderItemsResponse(
                        oi.getOrderItemId(),     // use snapshot columns on order_item
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getPriceAt()
                ))
                .toList();

        centralAudit.info(customerId, EndpointsNameMethods.ORDER_VIEW, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_VIEW_SUCCESS.getMessage());
        return orderServiceMapper.toDtoDetails(order, itemDtos);
    }
}