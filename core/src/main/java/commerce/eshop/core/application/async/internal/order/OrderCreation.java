package commerce.eshop.core.application.async.internal.order;

import commerce.eshop.core.application.checkout.writer.CheckoutWriter;
import commerce.eshop.core.application.events.order.PlacedOrderEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.factory.DefaultAddressFactory;
import commerce.eshop.core.application.order.factory.OrderFactory;
import commerce.eshop.core.application.order.validation.AuditedOrderValidation;
import commerce.eshop.core.application.order.writer.OrderCartWriter;
import commerce.eshop.core.application.order.writer.OrderWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.model.states.CheckoutStates;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class OrderCreation {

    // == Fields ==
    private final AuditedOrderValidation validation;
    private final DefaultAddressFactory defaultAddressFactory;
    private final OrderCartWriter orderCartWriter;
    private final CheckoutWriter checkoutWriter;
    private final OrderWriter orderWriter;
    private final OrderFactory orderFactory;
    private final DomainLookupService domainLookupService;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public OrderCreation(AuditedOrderValidation validation, DefaultAddressFactory defaultAddressFactory, OrderCartWriter orderCartWriter, CheckoutWriter checkoutWriter, OrderWriter orderWriter, OrderFactory orderFactory, DomainLookupService domainLookupService, ApplicationEventPublisher publisher) {
        this.validation = validation;
        this.defaultAddressFactory = defaultAddressFactory;
        this.orderCartWriter = orderCartWriter;
        this.checkoutWriter = checkoutWriter;
        this.orderWriter = orderWriter;
        this.orderFactory = orderFactory;
        this.domainLookupService = domainLookupService;
        this.publisher = publisher;
    }

    // == Public Methods ==

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOrder(long jobId){

        CheckoutJob job = domainLookupService.getCheckoutJob(jobId, "CREATE_ORDER");

        if(job.getState() != CheckoutStates.PROCESSING){
            return;
        }

        DTOOrderCustomerAddress address = job.getCustomerAddress() == null ? defaultAddressFactory.handle(job.getCustomerId()) : toDto(job.getCustomerAddress());

        final Customer customer = domainLookupService.getCustomerOrThrow(job.getCustomerId(), EndpointsNameMethods.ORDER_PLACE);
        final Cart cart = domainLookupService.getCartOrThrow(job.getCustomerId(), EndpointsNameMethods.ORDER_PLACE);

        // ->  serialize SAME-CART checkouts only, guard against other retries
        if (!orderCartWriter.lockCart(cart.getCartId())){
            throw new CannotSerializeTransactionException("cart checkout locked");
        }

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = orderCartWriter.sumCartOutstanding(cart.getCartId());
        validation.checkOutstanding(total_outstanding, job.getCustomerId()); // -> Check total outstanding > 0 / else fail
        Order order = orderFactory.handle(job.getOrderId(), customer, address, total_outstanding);

        // Decrement quantity of products based on the cart
        orderCartWriter.reserveStock(cart.getCartId(), job.getCustomerId());

        // Save order and flush for forced check + generate UUID.
        order = orderWriter.save(order, cart.getCartId(), job.getCustomerId());
        job.setState(CheckoutStates.ORDER_CREATED);
        job = checkoutWriter.save(job);
        PlacedOrderEvent orderEvent = new PlacedOrderEvent(job.getCustomerId(), order.getOrderId(), order.getTotalOutstanding(), "Euro");
        publisher.publishEvent(orderEvent);
    }

    private DTOOrderCustomerAddress toDto(Map<String, Object> address){
        return new DTOOrderCustomerAddress(address.get("country").toString(), address.get("street").toString(), address.get("city").toString(), address.get("postalCode").toString());
    }
}
