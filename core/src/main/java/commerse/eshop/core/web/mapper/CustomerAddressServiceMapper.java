package commerse.eshop.core.web.mapper;

import commerse.eshop.core.model.entity.CustomerAddress;
import commerse.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerAddressServiceMapper {

    public DTOCustomerAddressResponse toDto(CustomerAddress a) {
        return new DTOCustomerAddressResponse(
                a.getAddrId(),
                a.getCountry(),
                a.getStreet(),
                a.getCity(),
                a.getPostalCode(),
                a.isDefault()
        );}
}
