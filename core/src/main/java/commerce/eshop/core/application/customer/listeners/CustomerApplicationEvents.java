package commerce.eshop.core.application.customer.listeners;

import commerce.eshop.core.application.email.EmailComposer;
import commerce.eshop.core.application.events.EmailEventRequest;
import commerce.eshop.core.application.events.customer.CustomerRegisteredEvent;
import commerce.eshop.core.application.events.customer.CustomerFailedUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerSuccessfulUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerUpdatedInfoEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.service.DomainLookupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Component
@Slf4j
public class CustomerApplicationEvents {

    // == Fields ==
    private final EmailComposer emailComposer;
    private final ApplicationEventPublisher publisher;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public CustomerApplicationEvents(EmailComposer emailComposer, ApplicationEventPublisher publisher, DomainLookupService domainLookupService) {
        this.emailComposer = emailComposer;
        this.publisher = publisher;
        this.domainLookupService = domainLookupService;
    }

    // == Public Methods ==
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CustomerRegisteredEvent event){
        try {
            Customer customer = domainLookupService.getCustomerOrThrow(event.customerId(), "Placeholder");
            EmailEventRequest publishMailEvent = emailComposer.accountCreated(customer, "https://example.com/verify?token=...");
            log.info("Registration email: customer {} was send", event.customerId());
            publisher.publishEvent(publishMailEvent);
        } catch (NoSuchElementException ex){
            log.warn("Skip registration email: customer {} not found", event.customerId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CustomerUpdatedInfoEvent event){
        Customer customer = domainLookupService.getCustomerOrThrow(event.customerId(), "Placeholder");
        EmailEventRequest publishMailEvent = emailComposer.accountUpdated(customer, event.changed());
        publisher.publishEvent(publishMailEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void on(CustomerFailedUpdatePasswordEvent event){
        Customer customer = domainLookupService.getCustomerOrThrow(event.customerId(), "Placeholder");
        EmailEventRequest publishMailEvent = emailComposer.passwordUpdated(customer, event.successfulOrNot());
        publisher.publishEvent(publishMailEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CustomerSuccessfulUpdatePasswordEvent event){
        Customer customer = domainLookupService.getCustomerOrThrow(event.customerId(), "Placeholder");
        EmailEventRequest publishMailEvent = emailComposer.passwordUpdated(customer, event.successfulOrNot());
        publisher.publishEvent(publishMailEvent);
    }
}
