package commerce.eshop.core.application.customer.commands;

import commerce.eshop.core.application.events.customer.CustomerRegisteredEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.application.customer.factory.CustomerFactory;
import commerce.eshop.core.application.customer.writer.CustomerWriter;
import commerce.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class CustomerRegistration {

    // == Fields ==
    private final CustomerFactory customerFactory;
    private final CustomerWriter customerWriter;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public CustomerRegistration(CustomerFactory customerFactory, CustomerWriter customerWriter, ApplicationEventPublisher publisher) {
        this.customerFactory = customerFactory;
        this.customerWriter = customerWriter;
        this.publisher = publisher;
    }

    // == Public Methods ==
    @Transactional
    public Customer handle(DTOCustomerCreateUser dto){
        Customer customer = customerFactory.createFrom(dto);
        customer = customerWriter.save(customer); // Save to db and return the persisting customer back to get the generated ID.
        publisher.publishEvent(new CustomerRegisteredEvent(customer.getCustomerId()));
        return customer;
    }
}
