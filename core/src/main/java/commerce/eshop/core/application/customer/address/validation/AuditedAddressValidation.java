package commerce.eshop.core.application.customer.address.validation;

import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class AuditedAddressValidation {

    // == Fields ==
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public AuditedAddressValidation(CentralAudit centralAudit) {
        this.centralAudit = centralAudit;
    }

    // == Public Method ==

    public void checkOwnership(CustomerAddress addr, UUID customerId, String endpoint){
        if(!(addr.getCustomer().getCustomerId().equals(customerId))){
            throw centralAudit.audit(new NoSuchElementException("Mentioned address couldn't be found for user " + customerId),
                    customerId, endpoint, AuditingStatus.ERROR,
                    "Mentioned address couldn't be found for user " + customerId);
        }
    }
}
