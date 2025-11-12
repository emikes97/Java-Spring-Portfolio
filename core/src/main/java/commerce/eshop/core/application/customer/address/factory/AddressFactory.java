package commerce.eshop.core.application.customer.address.factory;

import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import org.springframework.stereotype.Component;

@Component
public class AddressFactory {

    // == Public Methods ==
    public CustomerAddress handle(DTOAddCustomerAddress dto, Customer customer){
        return  new CustomerAddress(customer, dto.country(), dto.street(), dto.city(),
                dto.postalCode(),dto.isDefault());
    }
}
