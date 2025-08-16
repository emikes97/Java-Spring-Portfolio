package commerse.eshop.core.service;

import commerse.eshop.core.model.entity.CustomerAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerAddressService {

    Page<CustomerAddress> getAllAddresses(UUID customerId, Pageable pageable);


}
