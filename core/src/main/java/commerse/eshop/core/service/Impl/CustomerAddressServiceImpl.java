package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.CustomerAddress;
import commerse.eshop.core.repository.CustomerAddrRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.service.CustomerAddressService;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import commerse.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class CustomerAddressServiceImpl implements CustomerAddressService {

    private final CustomerAddrRepo customerAddrRepo;
    private final CustomerRepo customerRepo;

    @Autowired
    protected CustomerAddressServiceImpl(CustomerAddrRepo customerAddrRepo, CustomerRepo customerRepo){
        this.customerAddrRepo = customerAddrRepo;
        this.customerRepo = customerRepo;
    }

    @Override
    public Page<DTOCustomerAddressResponse> getAllAddresses(UUID customerId, Pageable pageable) {
        return customerAddrRepo.findByCustomerCustomerId(customerId, pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public DTOCustomerAddressResponse addCustomerAddress(UUID customerId, DTOAddCustomerAddress dto) {
        CustomerAddress customerAddress = new CustomerAddress(customerRepo.findById(customerId).orElseThrow(()
                -> new RuntimeException("The customer doesn't exist")), dto.country(), dto.city(), dto.street(), dto.postalCode(), customerAddrRepo ? dto.isDefault() : false)
        return null;
    }

    @Override
    public DTOCustomerAddressResponse updateCustomerAddress(UUID customerId, Long id, DTOUpdateCustomerAddress dto) {
        return null;
    }

    @Override
    public DTOCustomerAddressResponse makeDefaultCustomerAddress(UUID customerId, Long id) {
        return null;
    }

    @Override
    @Transactional
    public void deleteCustomerAddress(UUID customerId, Long id) {
        long deleted = customerAddrRepo.deleteByAddressIdAndCustomer_CustomerId(id, customerId);

        if (deleted == 0){
            throw new NoSuchElementException("Address not found");
        }

        log.info("Address was deleted with id = " + id + " and customer id = " + customerId);
    }

    private DTOCustomerAddressResponse toDto(CustomerAddress a) {
        return new DTOCustomerAddressResponse(
                a.getAddrId(),
                a.getCountry(),
                a.getStreet(),
                a.getCity(),
                a.getPostalCode(),
                a.isDefault()
        );}
}
