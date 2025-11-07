package commerce.eshop.core.application.customer.events;

import commerce.eshop.core.email.EmailComposer;
import commerce.eshop.core.events.EmailEventRequest;
import commerce.eshop.core.events.customer.CustomerRegisteredEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.util.CentralAudit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Component
@Slf4j
public class RegistrationEvent {

    // == Fields ==
    private final EmailComposer emailComposer;
    private final ApplicationEventPublisher publisher;
    private final CustomerRepo customerRepo;

    // == Constructors ==
    @Autowired
    public RegistrationEvent(EmailComposer emailComposer, ApplicationEventPublisher publisher, CustomerRepo customerRepo,
                             CentralAudit centralAudit) {
        this.emailComposer = emailComposer;
        this.publisher = publisher;
        this.customerRepo = customerRepo;
    }

    // == Public Methods ==
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CustomerRegisteredEvent event){
        try {
            Customer customer = customerRepo.findById(event.customerId()).orElseThrow(
                    () -> new NoSuchElementException("Customer with id" + event.customerId() + " doesn't exist")
            );
            EmailEventRequest publishMailEvent = emailComposer.accountCreated(customer, "https://example.com/verify?token=...");
            log.info("Registration email: customer {} was send", event.customerId());
            publisher.publishEvent(publishMailEvent);
        } catch (NoSuchElementException ex){
            log.warn("Skip registration email: customer {} not found", event.customerId());
        }
    }
}
