package commerce.eshop.core.application;

import commerce.eshop.core.application.email.EmailComposer;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.repository.CartItemRepo;
import commerce.eshop.core.repository.DbLockRepository;
import commerce.eshop.core.repository.OrderItemRepo;
import commerce.eshop.core.repository.OrderRepo;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.enums.OrderStatus;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import commerce.eshop.core.web.mapper.OrderServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPlacementExecutor {

    /**
     * ONE ATTEMPT (transactional)
     */

    private final DomainLookupService domainLookupService;
    private final CartItemRepo cartItemRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CentralAudit centralAudit;
    private final OrderServiceMapper orderServiceMapper;
    private final EmailComposer emailComposer;
    private final ApplicationEventPublisher publisher;
    private final DbLockRepository dbLockRepo;


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTOOrderPlacedResponse tryPlaceOrder(UUID customerId, DTOOrderCustomerAddress addressDto) {

        // Validation of customerId
        if(customerId == null){
            IllegalArgumentException illegal = new IllegalArgumentException("The identification key can't be empty");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, illegal.toString());
        }

        // Get cart_id from customer
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);

        // serialize SAME-CART checkouts only
        if (!dbLockRepo.tryLockCart(cart.getCartId())){
            throw new CannotSerializeTransactionException("cart checkout locked");
        }

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

        // Decrement quantity of products based on the cart
        // IMPORTANT: let serialization/deadlock exceptions bubble to outer retry
        int updated = cartItemRepo.reserveStockForCart(cart.getCartId());
        long expected = cartItemRepo.countDistinctCartProducts(cart.getCartId());
        if(updated != expected){
            /// Client error, The product was out-of-stock when order was placed.
            ResponseStatusException err = new ResponseStatusException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK");
            throw centralAudit.audit(err, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.WARNING, "INSUFFICIENT_STOCK");
        }

        // Save order and flush
        try {
            orderRepo.saveAndFlush(order);
            // Snapshot cart items to order_items
            orderItemRepo.snapShotFromCart(order.getOrderId(), cart.getCartId());
            orderItemRepo.clearCart(cart.getCartId());
            centralAudit.info(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_PLACE_SUCCESS.getMessage());
            var event = emailComposer.orderConfirmed(customer, order, order.getTotalOutstanding(), "Euro");
            publisher.publishEvent(event);
            return orderServiceMapper.toDto(order);
        } catch (DataIntegrityViolationException dub){
            throw centralAudit.audit(dub, customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.ERROR, dub.toString());
        }
    }
}
