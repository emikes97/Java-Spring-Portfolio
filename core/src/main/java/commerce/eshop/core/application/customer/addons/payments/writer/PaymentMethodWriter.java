package commerce.eshop.core.application.customer.addons.payments.writer;

import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.repository.CustomerPaymentMethodRepo;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Component
public class PaymentMethodWriter {

    // == Fields ==
    private final CustomerPaymentMethodRepo customerPaymentMethodRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public PaymentMethodWriter(CustomerPaymentMethodRepo customerPaymentMethodRepo, CentralAudit centralAudit) {
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    public void updateDefaultToFalse(UUID customerId){
        int outcome = customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId);
        if (outcome == 0)
            log.info("Customer with customerId= " + customerId + " doesn't have a default method for payments");
    }

    public CustomerPaymentMethod save(CustomerPaymentMethod pm, String endpoint){
        try {
            pm = customerPaymentMethodRepo.saveAndFlush(pm);
            return pm;
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, pm.getCustomer().getCustomerId(), endpoint, AuditingStatus.ERROR, dup.toString());
        }
    }

    public long delete(UUID customerId, UUID paymentId){
        try {
            long outcome = customerPaymentMethodRepo
                    .deleteByCustomer_CustomerIdAndCustomerPaymentId(customerId, paymentId);
            if (outcome == 0) {
                // Not found case
                NoSuchElementException notFound =
                        new NoSuchElementException("Payment method not found: " + paymentId);
                throw centralAudit.audit(notFound, customerId, EndpointsNameMethods.PM_DELETE,
                        AuditingStatus.WARNING, notFound.toString());
            }
            return outcome;
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.PM_DELETE,
                    AuditingStatus.ERROR, dup.toString());
        }
    }
}
