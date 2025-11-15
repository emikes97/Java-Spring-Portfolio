package commerce.eshop.core.application.order.orchestrator;

import commerce.eshop.core.application.email.EmailComposer;
import commerce.eshop.core.application.events.order.PlacedOrderEvent;
import commerce.eshop.core.application.order.factory.DefaultAddressFactory;
import commerce.eshop.core.application.order.factory.OrderFactory;
import commerce.eshop.core.application.order.validation.AuditedOrderValidation;
import commerce.eshop.core.application.order.writer.CartWriter;
import commerce.eshop.core.application.order.writer.OrderWriter;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.repository.CartItemRepo;
import commerce.eshop.core.repository.DbLockRepository;
import commerce.eshop.core.repository.OrderItemRepo;
import commerce.eshop.core.repository.OrderRepo;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditMessage;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.OrderStatus;
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
public class OrderPlacementExecutor {

    /**
     * ONE ATTEMPT (transactional)
     */
    // == Field ==
    private final AuditedOrderValidation validation;
    private final DefaultAddressFactory defaultAddressFactory;
    private final CartWriter cartWriter;
    private final OrderWriter orderWriter;
    private final OrderFactory orderFactory;
    private final DomainLookupService domainLookupService;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==


    public OrderPlacementExecutor(AuditedOrderValidation validation, DefaultAddressFactory defaultAddressFactory, CartWriter cartWriter,
                                  OrderWriter orderWriter, OrderFactory orderFactory,
                                  DomainLookupService domainLookupService, ApplicationEventPublisher publisher) {
        this.validation = validation;
        this.defaultAddressFactory = defaultAddressFactory;
        this.cartWriter = cartWriter;
        this.orderWriter = orderWriter;
        this.orderFactory = orderFactory;
        this.domainLookupService = domainLookupService;
        this.publisher = publisher;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTOOrderPlacedResponse tryPlaceOrder(UUID customerId, DTOOrderCustomerAddress addressDto) {

        validation.checkCustomerId(customerId);
        final Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);

        // ->  serialize SAME-CART checkouts only, guard against other retries
        if (!cartWriter.lockCart(cart.getCartId())){
            throw new CannotSerializeTransactionException("cart checkout locked");
        }

        // -> If true, generate the default address from customer
        if(validation.checkAddressDto(addressDto))
            addressDto = defaultAddressFactory.handle(customerId);

        // #1 Sum total_outstanding from Cart_Items;
        BigDecimal total_outstanding = cartWriter.sumCartOutstanding(cart.getCartId());
        validation.checkOutstanding(total_outstanding, customerId); // -> Check total outstanding > 0 / else fail
        Order order = orderFactory.handle(customer, addressDto, total_outstanding);

        // Decrement quantity of products based on the cart
        // IMPORTANT: let serialization/deadlock exceptions bubble to outer retry
        cartWriter.reserveStock(cart.getCartId(), customerId);

        // Save order and flush
        order = orderWriter.save(order, cart.getCartId(), customerId);
        PlacedOrderEvent orderEvent = new PlacedOrderEvent(customerId, order.getOrderId(), order.getTotalOutstanding(), "Euro");
        publisher.publishEvent(orderEvent);
        return orderFactory.toDto(order);
    }
}
