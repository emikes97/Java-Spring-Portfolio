package commerce.eshop.core.application.customer.commands;

import commerce.eshop.core.application.customer.writer.CustomerWriter;
import commerce.eshop.core.application.events.customer.CustomerSuccessfulUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerUpdatedInfoEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.customer.validation.AuditedCustomerValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CustomerServiceActions {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final AuditedCustomerValidation validator;
    private final CustomerWriter customerWriter;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public CustomerServiceActions(DomainLookupService domainLookupService, AuditedCustomerValidation validator, CustomerWriter customerWriter,
            ApplicationEventPublisher publisher) {

        this.domainLookupService = domainLookupService;
        this.customerWriter = customerWriter;
        this.publisher = publisher;
        this.validator = validator;
    }

    // == Public Methods ==

    @Transactional
    public void handleUpdateName(UUID customerId, String password, String name){

        validator.verifyCustomer(customerId, EndpointsNameMethods.UPDATE_NAME);
        validator.requireNotBlank(name, customerId, EndpointsNameMethods.UPDATE_NAME, "INVALID_NAME", "Name must not be blank.");
        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_NAME);
        validator.verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_NAME);
        String trimmed = name.trim();

        if(validator.isNoChange(customer, trimmed, EndpointsNameMethods.UPDATE_NAME)){
            return;
        }

        customer.setName(trimmed);
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customer.getCustomerId(), "Name"));
    }

    @Transactional
    public void handleUpdateSurname(UUID customerId, String password, String lastName) {
        validator.verifyCustomer(customerId, EndpointsNameMethods.UPDATE_SURNAME);
        validator.requireNotBlank(lastName, customerId, EndpointsNameMethods.UPDATE_SURNAME, "INVALID_SURNAME", "Surname must not be blank.");
        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_SURNAME);
        validator.verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_SURNAME);

        String trimmed = lastName.trim();
        if (validator.isNoChange(customer, trimmed, EndpointsNameMethods.UPDATE_SURNAME)) {
            return;
        }

        customer.setSurname(trimmed);
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customer.getCustomerId(), "Surname"));
    }

    @Transactional
    public void handleUpdateFullName(UUID customerId, String password, String name, String lastName){
        validator.verifyCustomer(customerId, EndpointsNameMethods.UPDATE_FULLNAME);
        validator.requireNotBlank(name, customerId, EndpointsNameMethods.UPDATE_FULLNAME, "INVALID_FULLNAME", "Name must not be blank.");
        validator.requireNotBlank(lastName, customerId, EndpointsNameMethods.UPDATE_FULLNAME, "INVALID_FULLNAME", "Surname must not be blank.");
        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_FULLNAME);
        validator.verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_FULLNAME);

        String newName = name.trim();
        String newSurname = lastName.trim();

        boolean changed = false;
        if (!validator.isNoChange(customer, newName, EndpointsNameMethods.UPDATE_NAME))
        {
            customer.setName(newName);
            changed = true;
        }
        if(!validator.isNoChange(customer, newSurname, EndpointsNameMethods.UPDATE_SURNAME)){
            customer.setSurname(newSurname);
            changed = true;
        }

        if (!changed) {
            validator.auditNoChange(customerId);
            return;
        }

        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customerId, "Name & Surname"));
    }

    @Transactional
    public void handleUpdateUserName(UUID customerId, String password, String userName) {
        validator.verifyCustomer(customerId, EndpointsNameMethods.UPDATE_USERNAME);
        validator.requireNotBlank(userName, customerId, EndpointsNameMethods.UPDATE_USERNAME, "INVALID_USERNAME", "Username must not be blank.");
        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_USERNAME);
        validator.verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_USERNAME);

        String trimmed = userName.trim();
        if(validator.isNoChange(customer, trimmed, EndpointsNameMethods.UPDATE_USERNAME)) {
            return;
        }

        customer.setUsername(trimmed);
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customerId, "Username"));
    }

    @Transactional
    public void handleUpdateUserPassword(UUID customerId, String currentPassword, String newPassword) {

        validator.verifyCustomer(customerId, EndpointsNameMethods.UPDATE_PASSWORD);
        validator.requireNotBlank(currentPassword, customerId, EndpointsNameMethods.UPDATE_PASSWORD, "INVALID_INPUT", "Missing current password.");
        validator.requireNotBlank(newPassword,     customerId, EndpointsNameMethods.UPDATE_PASSWORD, "INVALID_INPUT", "Missing new password.");
        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_PASSWORD);
        validator.verifyPasswordOrThrow(customer, currentPassword, customerId, EndpointsNameMethods.UPDATE_PASSWORD);

        // Reject easy passwords
        validator.verifyPasswordIntegrity(newPassword, customerId);

        // Prevent reuse -- If it's not duplicate return new hashed password
        String hashedPassword = validator.verifyPasswordDuplication(newPassword, customer);

        customer.setPasswordHash(hashedPassword);
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerSuccessfulUpdatePasswordEvent(customerId, true));
    }
}
