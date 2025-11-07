package commerce.eshop.core.application.customer.writer;

import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerWriter {

    // == Fields ==
    private final CustomerRepo customerRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==

    public CustomerWriter(CustomerRepo customerRepo, CentralAudit centralAudit) {
        this.customerRepo = customerRepo;
        this.centralAudit = centralAudit;
    }

    // == Public methods ==

    public Customer save(Customer customer){

        try {
            return customerRepo.saveAndFlush(customer);
        } catch (DataIntegrityViolationException dup) {
            log.warn("CREATE_USER failed (duplicate/constraint) email={} phone={}", customer.getEmail(), customer.getPhoneNumber(), dup);
            throw centralAudit.audit(dup, null, EndpointsNameMethods.CREATE_USER, AuditingStatus.ERROR, dup.toString());
        }
    }
}
