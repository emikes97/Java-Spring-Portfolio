package commerce.eshop.core.application.customer.validation;

import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditedCustomerValidation {

    // == Fields ==
    private final CentralAudit centralAudit;
    private final PasswordEncoder passwordEncoder;

    // == Constructors ==

    @Autowired
    public AuditedCustomerValidation(CentralAudit centralAudit, PasswordEncoder passwordEncoder) {
        this.centralAudit = centralAudit;
        this.passwordEncoder = passwordEncoder;
    }

    // == Public Methods ==

    /** Fail if the value is blank. Audits with WARNING and throws 400 (IllegalArgumentException). */
    public void requireNotBlank(String val, UUID cid, String endpoint, String code, String msg) {
        if (val == null || val.isBlank()) {
            throw centralAudit.audit(new IllegalArgumentException(msg), cid, endpoint, AuditingStatus.WARNING, code);
        }
    }

    /** Verify password or audit+throw 401 (BadCredentialsException). */
    public void verifyPasswordOrThrow(Customer c, String raw, UUID cid, String method) {
        if (raw == null || !passwordEncoder.matches(raw, c.getPasswordHash())) {
            throw centralAudit.audit(new BadCredentialsException("Invalid password"), cid, method, AuditingStatus.WARNING, "INVALID_PASSWORD");
        }
    }

    /** If new password is the same as the old, fail **/
    public String verifyPasswordDuplication(String newPassword, Customer customer){

        if (passwordEncoder.matches(newPassword, customer.getPasswordHash())) {
            IllegalArgumentException illegal = new IllegalArgumentException("New password must be different from current.");
            throw centralAudit.audit(illegal, customer.getCustomerId(), EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "REUSED_PASSWORD");
        }

        return passwordEncoder.encode(newPassword);
    }

    /** Decline any password less than 8 characters **/
    public void verifyPasswordIntegrity(String newPassword, UUID customerId){
        if (newPassword.length() < 8) {
            IllegalArgumentException illegal = new IllegalArgumentException("Password too short.");
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "WEAK_PASSWORD");
        }
    }

    /** Decline any null customerId **/
    public void verifyCustomer(UUID customerId, String endpoint){
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, endpoint, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }
    }

    /** Verify new name/surname **/
    public boolean isNoChange(Customer customer, String trimmed, String methodName){

        switch (methodName) {
            case EndpointsNameMethods.UPDATE_NAME -> {
                if (trimmed.equals(customer.getName())) {
                    centralAudit.info(customer.getCustomerId(), methodName, AuditingStatus.WARNING, "NO_CHANGE_SAME_NAME");
                    return true;
                }
                return false;
            }
            case EndpointsNameMethods.UPDATE_SURNAME -> {
                if (trimmed.equals(customer.getSurname())) {
                    centralAudit.info(customer.getCustomerId(), methodName, AuditingStatus.WARNING, "NO_CHANGE_SAME_SURNAME");
                    return true;
                }
                return false;
            }
            case EndpointsNameMethods.UPDATE_USERNAME -> {
                if (trimmed.equals(customer.getUsername())) {
                    centralAudit.info(customer.getCustomerId(), methodName, AuditingStatus.WARNING, "NO_CHANGE_SAME_USERNAME");
                    return true;
                }
                return false;
            }
            default -> {
                IllegalArgumentException illegal = new IllegalArgumentException("Non-existant method");
                throw  centralAudit.audit(illegal, customer.getCustomerId(), "UNREGISTERED_METHOD", AuditingStatus.ERROR,
                        "Method passed at isNoChange, doesn't exist");
            }
        }
    }

    // ** current purpose a single use till I rework the code for updating fullname ** //
    public void auditNoChange(UUID customerId){
        centralAudit.info(customerId, EndpointsNameMethods.UPDATE_FULLNAME, AuditingStatus.WARNING, "NO_CHANGE_SAME_FULLNAME");
    }
}
