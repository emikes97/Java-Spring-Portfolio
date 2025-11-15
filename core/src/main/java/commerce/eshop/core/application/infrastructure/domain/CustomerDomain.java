package commerce.eshop.core.application.infrastructure.domain;

import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.repository.CustomerAddrRepo;
import commerce.eshop.core.repository.CustomerPaymentMethodRepo;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class CustomerDomain {

    // == Fields ==
    private final CustomerRepo cRepo;
    private final CustomerAddrRepo aRepo;
    private final CustomerPaymentMethodRepo pRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public CustomerDomain(CustomerRepo cRepo, CustomerAddrRepo aRepo, CustomerPaymentMethodRepo pRepo, CentralAudit centralAudit) {
        this.cRepo = cRepo;
        this.aRepo = aRepo;
        this.pRepo = pRepo;
        this.centralAudit = centralAudit;
    }

    // == Public methods ==
    public Customer retrieveCustomer(UUID customerId, String method){
        try {
            return cRepo.findById(customerId).orElseThrow(
                    () -> new NoSuchElementException("Customer doesn't exist")
            );
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    public Customer retrieveCustomerByPhoneOrMail(String key, String method){
        try {
            return cRepo.findByPhoneNumberOrEmail(key)
                    .orElseThrow(() -> new NoSuchElementException("Customer not found for: " + key));
        } catch (NoSuchElementException e) {
            throw centralAudit.audit(e,null, method,
                    AuditingStatus.WARNING, "CUSTOMER_NOT_FOUND:" + key);
        }
    }

    public CustomerAddress retrieveCustomerAddress(UUID customerId, long id, String method){
        try {
            return aRepo.findByAddrIdAndCustomer_CustomerId(id, customerId).orElseThrow(() -> new NoSuchElementException("The address doesn't exist."));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    public CustomerAddress retrieveDefaultAddress(UUID customerId, String method){
        try {
            return  aRepo.findByCustomerCustomerIdAndIsDefaultTrue(customerId).orElseThrow(
                    () -> new NoSuchElementException("The customer = " + customerId + " doesn't have a default address and " +
                            "no address has been provided for the order"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    public CustomerPaymentMethod retrieveCustomerPaymentMethod(UUID customerId, UUID paymentMethodId, String method){
        try {
            return pRepo.findByCustomer_CustomerIdAndCustomerPaymentId(
                    customerId, paymentMethodId).orElseThrow(
                    () -> new NoSuchElementException("The payment method doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    // == Pageable Methods ==
    public Page<CustomerAddress> retrievePagedCustomerAddress(UUID customerId, Pageable page){
        return aRepo.findByCustomerCustomerId(customerId, page);
    }

    public Page<CustomerPaymentMethod> retrievePagedCustomerPaymentMethod(UUID customerId, Pageable page){
        return pRepo.findByCustomer_CustomerId(customerId, page);
    }
}
