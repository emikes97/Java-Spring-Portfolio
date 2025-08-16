package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.CustomerAddress;
import commerse.eshop.core.repository.CustomerAddrRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.service.CustomerAddressService;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import commerse.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public Page<DTOCustomerAddressResponse> getAllAddresses(UUID customerId, Pageable pageable) {
        return customerAddrRepo.findByCustomerCustomerId(customerId, pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public DTOCustomerAddressResponse addCustomerAddress(UUID customerId, DTOAddCustomerAddress dto) {

        if (dto.isDefault()) {
            long outcome = customerAddrRepo.clearDefaultsForCustomer(customerId);
            log.debug(String.valueOf(outcome));
            if (outcome == 0)
                log.info("There wasn't a default address");
        }

        // get a reference (no DB hit) just to set the FK
        var customerRef = customerRepo.getReferenceById(customerId);

        CustomerAddress customerAddress = new CustomerAddress(customerRef, dto.country(), dto.street(), dto.city(),
                dto.postalCode(),dto.isDefault());

        customerAddrRepo.save(customerAddress);

        return toDto(customerAddress);
    }

    @Transactional
    @Override
    public DTOCustomerAddressResponse updateCustomerAddress(UUID customerId, Long id, DTOUpdateCustomerAddress dto) {

        CustomerAddress addr = customerAddrRepo.findById(id).orElseThrow(() -> new RuntimeException("The address doesn't exist."));

        if(!(addr.getCustomer().getCustomerId().equals(customerId)))
            throw new NoSuchElementException("Mentioned address couldn't be found for user " + customerId);

        if (dto.country() != null && !dto.country().isBlank())
            addr.setCountry(dto.country());
        if (dto.city() != null && !dto.city().isBlank())
            addr.setCity(dto.city());
        if (dto.street() != null && !dto.street().isBlank())
            addr.setStreet(dto.street());
        if (dto.postalCode() != null && !dto.postalCode().isBlank())
            addr.setPostalCode(dto.postalCode());

        if (dto.isDefault() != null){
            if (dto.isDefault()){
                customerAddrRepo.clearDefaultsForCustomer(customerId);

                addr.setDefault(true);
            } else{
                addr.setDefault(false);
            }
        }

        customerAddrRepo.save(addr);

        return toDto(addr);
    }

    @Transactional
    @Override
    public DTOCustomerAddressResponse makeDefaultCustomerAddress(UUID customerId, Long id) {

        CustomerAddress customerAddress = customerAddrRepo.findByAddressIdAndCustomer_CustomerId(id, customerId).orElseThrow(
                () -> new RuntimeException("Customer or Address doesn't exist.")
        );

        customerAddrRepo.clearDefaultsForCustomer(customerId);

        customerAddress.setDefault(true);

        customerAddrRepo.save(customerAddress);

        return toDto(customerAddress);
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
