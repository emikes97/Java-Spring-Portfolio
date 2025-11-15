package commerce.eshop.core.application.order.factory;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultAddressFactory {

    // == Fields ==
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public DefaultAddressFactory(DomainLookupService domainLookupService) {
        this.domainLookupService = domainLookupService;
    }

    // == Public Method ==
    public DTOOrderCustomerAddress handle(UUID customerId){
        final CustomerAddress customerAddress = domainLookupService.getCustomerAddrOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);
         return new DTOOrderCustomerAddress(customerAddress.getCountry(), customerAddress.getStreet(),
                 customerAddress.getCity(), customerAddress.getPostalCode());
    }
}
