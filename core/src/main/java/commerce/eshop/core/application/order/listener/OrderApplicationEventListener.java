package commerce.eshop.core.application.order.listener;

import commerce.eshop.core.application.async.internal.payment.confirmPayment.ConfirmPaymentOrchestrator;
import commerce.eshop.core.application.email.EmailComposer;
import commerce.eshop.core.application.events.email.EmailEventRequest;
import commerce.eshop.core.application.events.order.CancelledOrderEvent;
import commerce.eshop.core.application.events.order.PlacedOrderEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class OrderApplicationEventListener {

    // == Fields ==
    private final EmailComposer emailComposer;
    private final ConfirmPaymentOrchestrator confirmPaymentOrchestrator;
    private final ApplicationEventPublisher publisher;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public OrderApplicationEventListener(EmailComposer emailComposer, ConfirmPaymentOrchestrator confirmPaymentOrchestrator,
                                         ApplicationEventPublisher publisher, DomainLookupService domainLookupService) {
        this.emailComposer = emailComposer;
        this.confirmPaymentOrchestrator = confirmPaymentOrchestrator;
        this.publisher = publisher;
        this.domainLookupService = domainLookupService;
    }
    // == Public Methods ==

    @Async("transactionExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPayment(PlacedOrderEvent event){
        confirmPaymentOrchestrator.handle(event.customerId(), event.orderId(), event.idemkey(), event.jobId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PlacedOrderEvent event){
        Order order = getOrder(event.customerId(), event.orderId());
        Customer customer = getCustomer(event.customerId());
        EmailEventRequest emailEvent = emailComposer.orderConfirmed(customer, order, event.total_outstanding(), event.currency());
        publisher.publishEvent(emailEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CancelledOrderEvent event){
        Order order = getOrder(event.customerId(), event.orderId());
        Customer customer = getCustomer(event.customerId());
        EmailEventRequest emailEvent = emailComposer.orderCancelled(customer, order);
        publisher.publishEvent(emailEvent);
    }


    // == Private Methods ==

    private Order getOrder(UUID customerId, UUID orderId){
        return domainLookupService.getOrderOrThrow(customerId, orderId, "Placeholder");
    }

    private Customer getCustomer(UUID customerId){
        return domainLookupService.getCustomerOrThrow(customerId, "Placeholder");
    }

}
