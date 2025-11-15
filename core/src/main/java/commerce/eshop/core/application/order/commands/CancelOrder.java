package commerce.eshop.core.application.order.commands;

import commerce.eshop.core.application.events.order.CancelledOrderEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.validation.AuditedOrderValidation;
import commerce.eshop.core.application.order.writer.OrderWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.model.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CancelOrder {

    // == Fields ==
    private final AuditedOrderValidation validation;
    private final DomainLookupService domainLookupService;
    private final OrderWriter orderWriter;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public CancelOrder(AuditedOrderValidation validation, DomainLookupService domainLookupService, OrderWriter orderWriter, ApplicationEventPublisher publisher) {
        this.validation = validation;
        this.domainLookupService = domainLookupService;
        this.orderWriter = orderWriter;
        this.publisher = publisher;
    }

    // == Public Methods ==
    @Transactional
    public void handle(UUID customerId, UUID orderId){

        validation.checkCustomerAndOrder(customerId, orderId); // -> If either is missing fail early
        Order order = domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_CANCEL);
        validation.checkOrderStatus(order.getOrderStatus(), customerId); // -> If order is in terminal state, fail

        int expected = orderWriter.countOrderItems(order.getOrderId());
        validation.checkExpectedUpdated(expected, customerId); // -> If expected == 0 fail
        int updated = orderWriter.restoreProductStock(order.getOrderId());
        validation.checkRestockUpdate(updated, expected, customerId); // -> If update != expected fail & rollback data mismatch

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderWriter.save(order, customerId, EndpointsNameMethods.ORDER_CANCEL);

        CancelledOrderEvent event = new CancelledOrderEvent(customerId, order.getOrderId());
        publisher.publishEvent(event);
    }
}
