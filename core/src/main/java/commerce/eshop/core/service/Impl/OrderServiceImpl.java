package commerce.eshop.core.service.Impl;

import commerce.eshop.core.email.EmailComposer;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.repository.*;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.service.Components.OrderPlacementExecutor;
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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
    private final ApplicationEventPublisher publisher;
    private final EmailComposer emailComposer;
    private final OrderPlacementExecutor executor; // <-- inject the separate bean

    // == Constructors ==
    @Autowired
    public OrderServiceImpl(CustomerRepo customerRepo, CartItemRepo cartItemRepo, CartRepo cartRepo, OrderItemRepo orderItemRepo,
                            OrderRepo orderRepo, CustomerAddrRepo customerAddrRepo, CustomerPaymentMethodRepo customerPaymentMethodRepo,
                            CentralAudit centralAudit, OrderServiceMapper orderServiceMapper, DomainLookupService domainLookupService,
                            ApplicationEventPublisher publisher, EmailComposer emailComposer, OrderPlacementExecutor executor){

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
        this.emailComposer = emailComposer;
        this.publisher = publisher;
        this.executor = executor;
    }

    // == Public Methods ==

    /**
     * ENTRYPOINT (non-transactional): bounded retry with small random backoff.
     * Controller keeps calling this; signature unchanged.
     */
    @Override
    public DTOOrderPlacedResponse placeOrder(UUID customerId, DTOOrderCustomerAddress addressDto) {
        int attempts = 0;
        while (true) {
            try {
                // Each call opens a FRESH transaction
                return  executor.tryPlaceOrder(customerId, addressDto);
            } catch (CannotSerializeTransactionException | DeadlockLoserDataAccessException ex) {
                if (++attempts >= 5) {
                    throw ex;
                }
                int backoffMs = ThreadLocalRandom.current().nextInt(5, 25);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while retrying order placement", ie);
                }
            }
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
            Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
            var event = emailComposer.orderCancelled(customer, order);
            publisher.publishEvent(event);
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