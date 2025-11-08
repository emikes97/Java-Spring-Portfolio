package commerce.eshop.core.application.customer.commands;

import commerce.eshop.core.application.customer.writer.CustomerWriter;
import commerce.eshop.core.application.events.customer.CustomerFailedUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerSuccessfulUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerUpdatedInfoEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CustomerServiceActions {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final CentralAudit centralAudit;
    private final CustomerWriter customerWriter;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public CustomerServiceActions(DomainLookupService domainLookupService, CentralAudit centralAudit, CustomerWriter customerWriter,
                                  PasswordEncoder passwordEncoder, ApplicationEventPublisher publisher) {
        this.domainLookupService = domainLookupService;
        this.centralAudit = centralAudit;
        this.customerWriter = customerWriter;
        this.passwordEncoder =  passwordEncoder;
        this.publisher = publisher;
    }

    // == Public Methods ==

    @Transactional
    public void handleUpdateName(UUID customerId, String password, String name){

        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_NAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(name, customerId, EndpointsNameMethods.UPDATE_NAME, "INVALID_NAME", "Name must not be blank.");

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_NAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_NAME);

        String trimmed = name.trim();
        if (trimmed.equals(customer.getName())) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_NAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_NAME");
            return;
        }

        customer.setName(trimmed);
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customer.getCustomerId(), "Name"));
    }

    @Transactional
    public void handleUpdateSurname(UUID customerId, String password, String lastName) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_SURNAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(lastName, customerId, EndpointsNameMethods.UPDATE_SURNAME, "INVALID_SURNAME", "Surname must not be blank.");
        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_SURNAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_SURNAME);

        String trimmed = lastName.trim();
        if (trimmed.equals(customer.getSurname())) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_SURNAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_SURNAME");
            return;
        }
        customer.setSurname(trimmed);
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customer.getCustomerId(), "Surname"));
    }

    @Transactional
    public void handleUpdateFullName(UUID customerId, String password, String name, String lastName){
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal,null, EndpointsNameMethods.UPDATE_FULLNAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(name, customerId, EndpointsNameMethods.UPDATE_FULLNAME, "INVALID_FULLNAME", "Name must not be blank.");
        requireNotBlank(lastName, customerId, EndpointsNameMethods.UPDATE_FULLNAME, "INVALID_FULLNAME", "Surname must not be blank.");

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_FULLNAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_FULLNAME);

        String newName = name.trim();
        String newSurname = lastName.trim();

        boolean changed = false;
        if (!newName.equals(customer.getName())) { customer.setName(newName); changed = true; }
        if (!newSurname.equals(customer.getSurname())) { customer.setSurname(newSurname); changed = true; }

        if (!changed) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_FULLNAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_FULLNAME");
            return;
        }

        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customerId, "Name & Surname"));
    }

    @Transactional
    public void handleUpdateUserName(UUID customerId, String password, String userName) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_USERNAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }
        requireNotBlank(userName, customerId, EndpointsNameMethods.UPDATE_USERNAME, "INVALID_USERNAME", "Username must not be blank.");

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_USERNAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_USERNAME);

        String trimmed = userName.trim();
        if (trimmed.equals(customer.getUsername())) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_USERNAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_USERNAME");
            return;
        }

        customer.setUsername(trimmed);
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerUpdatedInfoEvent(customerId, "Username"));
    }

    @Transactional
    public void handleUpdateUserPassword(UUID customerId, String currentPassword, String newPassword) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(currentPassword, customerId, EndpointsNameMethods.UPDATE_PASSWORD, "INVALID_INPUT", "Missing current password.");
        requireNotBlank(newPassword,     customerId, EndpointsNameMethods.UPDATE_PASSWORD, "INVALID_INPUT", "Missing new password.");

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_PASSWORD);
        verifyPasswordOrThrow(customer, currentPassword, customerId, EndpointsNameMethods.UPDATE_PASSWORD);

        // Reject easy passwords
        if (newPassword.length() < 8) {
            IllegalArgumentException illegal = new IllegalArgumentException("Password too short.");
            publisher.publishEvent(new CustomerFailedUpdatePasswordEvent(customerId, false));
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "WEAK_PASSWORD");
        }

        // Prevent reuse
        if (passwordEncoder.matches(newPassword, customer.getPasswordHash())) {
            IllegalArgumentException illegal = new IllegalArgumentException("New password must be different from current.");
            publisher.publishEvent(new CustomerFailedUpdatePasswordEvent(customerId, false));
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "REUSED_PASSWORD");
        }

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerWriter.save(customer);
        publisher.publishEvent(new CustomerSuccessfulUpdatePasswordEvent(customerId, true));
    }

        // == Private Methods ==

    /** Fail if the value is blank. Audits with WARNING and throws 400 (IllegalArgumentException). */
    private void requireNotBlank(String val, UUID cid, String endpoint, String code, String msg) {
        if (val == null || val.isBlank()) {
            throw centralAudit.audit(new IllegalArgumentException(msg), cid, endpoint, AuditingStatus.WARNING, code);
        }
    }

    /** Verify password or audit+throw 401 (BadCredentialsException). */
    private void verifyPasswordOrThrow(Customer c, String raw, UUID cid, String method) {
        if (raw == null || !passwordEncoder.matches(raw, c.getPasswordHash())) {
            throw centralAudit.audit(new BadCredentialsException("Invalid password"), cid, method, AuditingStatus.WARNING, "INVALID_PASSWORD");
        }
    }
}
