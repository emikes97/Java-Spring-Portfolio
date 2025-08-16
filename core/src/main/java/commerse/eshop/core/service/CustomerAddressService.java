package commerse.eshop.core.service;

import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import commerse.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerAddressService {

    // == Return all addresses of the customer (Serialized)
    Page<DTOCustomerAddressResponse> getAllAddresses(UUID customerId, Pageable pageable);

    // == Add new customer Address
    DTOCustomerAddressResponse addCustomerAddress(UUID customerId, DTOAddCustomerAddress dto);

    // == Update existing customer Address
    DTOCustomerAddressResponse updateCustomerAddress(UUID customerId, Long id, DTOUpdateCustomerAddress dto);

    // == Update address to default, set previous default to false.
    DTOCustomerAddressResponse makeDefaultCustomerAddress(UUID customerId, Long id);

    // == Delete Address
    void deleteCustomerAddress(UUID customerId, Long id);
}
