package commerce.eshop.core.application.customer.address.writer;

import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.repository.CustomerAddrRepo;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class AddressWriter {

    // == Fields ==
    private final CustomerAddrRepo customerAddrRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==

    @Autowired
    public AddressWriter(CustomerAddrRepo customerAddrRepo, CentralAudit centralAudit) {
        this.customerAddrRepo = customerAddrRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public int clearDefault(UUID customerId){
        return customerAddrRepo.clearDefaultsForCustomer(customerId);
    }

    public CustomerAddress save(CustomerAddress address, UUID customerId, String endpoint){
        try {
            customerAddrRepo.saveAndFlush(address);
            return address;
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, endpoint, AuditingStatus.ERROR, dup.toString());
        }
    }

    public long delete(UUID customerId, Long id){
        try {
            long deleted = customerAddrRepo.deleteByAddrIdAndCustomer_CustomerId(id, customerId);
            customerAddrRepo.flush();
            if (deleted == 0){
                centralAudit.warn(customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.WARNING, "Address not found");
                log.warn("No address to delete: id={} customerId={}", id, customerId);
                return deleted;
            }
            return deleted;
        } catch (DataIntegrityViolationException dive){
            // If address is referenced by something, deletion can fail
            Throwable most = dive.getMostSpecificCause();
            String msg = (most.getMessage() != null && !most.getMessage().isBlank()) ? most.getMessage() : dive.toString();
            throw centralAudit.audit(dive, customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.ERROR, msg);
        }
    }
}
