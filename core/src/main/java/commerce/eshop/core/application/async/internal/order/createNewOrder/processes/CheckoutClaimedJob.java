package commerce.eshop.core.application.async.internal.order.createNewOrder.processes;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.factory.DefaultAddressFactory;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CheckoutClaimedJob {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final DefaultAddressFactory defaultAddressFactory;

    // == Constructors ==
    @Autowired
    public CheckoutClaimedJob(DomainLookupService domainLookupService, DefaultAddressFactory defaultAddressFactory) {
        this.domainLookupService = domainLookupService;
        this.defaultAddressFactory = defaultAddressFactory;
    }

    // == Public Methods ==

    public CheckoutJob fetchJob(long id){
        return domainLookupService.getCheckoutJob(id, "FETCH_JOB -> ORDER_CREATION");
    }

    public DTOOrderCustomerAddress fetchAddress(CheckoutJob job){
        return job.getCustomerAddress() == null ? defaultAddressFactory.handle(job.getCustomerId()) : toDto(job.getCustomerAddress());
    }

    public Customer fetchCustomer(UUID customerId){
        return domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);
    }

    // == Private Methods ==
    private DTOOrderCustomerAddress toDto(Map<String, Object> address){
        return new DTOOrderCustomerAddress(address.get("country").toString(), address.get("street").toString(), address.get("city").toString(), address.get("postalCode").toString());
    }
}
