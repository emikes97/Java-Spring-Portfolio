package commerce.eshop.core.application.customer.addons.address.commands;

import commerce.eshop.core.application.customer.addons.address.factory.AddressFactory;
import commerce.eshop.core.application.customer.addons.address.validation.AuditedAddressValidation;
import commerce.eshop.core.application.customer.addons.address.writer.AddressWriter;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class CustomerAddressActions {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final AuditedAddressValidation validation;
    private final AddressWriter addressWriter;
    private final AddressFactory factory;

    // == Constructors ==
    @Autowired
    public CustomerAddressActions(DomainLookupService domainLookupService, AuditedAddressValidation validation, AddressWriter addressWriter, AddressFactory factory) {
        this.domainLookupService = domainLookupService;
        this.validation = validation;
        this.addressWriter = addressWriter;
        this.factory = factory;
    }

    // == Public Methods ==

    @Transactional
    public CustomerAddress addNewCustomerAddress(UUID customerId, DTOAddCustomerAddress dto) {

        final Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ADDR_ADD);

        prepareDefaultAssignment(dto, customerId);

        CustomerAddress customerAddress = factory.handle(dto, customer);
        customerAddress = addressWriter.save(customerAddress, customerId, EndpointsNameMethods.ADDR_ADD);
        return customerAddress;
    }

    @Transactional
    public CustomerAddress updateExistingAddress(UUID customerId, Long id, DTOUpdateCustomerAddress dto){
        CustomerAddress addr = domainLookupService.getCustomerAddrOrThrow(customerId, id, EndpointsNameMethods.ADDR_UPDATE);
        validation.checkOwnership(addr, customerId, EndpointsNameMethods.ADDR_UPDATE);

        // == Update fields ==
        if (dto.country() != null && !dto.country().isBlank())
            addr.setCountry(dto.country());
        if (dto.city() != null && !dto.city().isBlank())
            addr.setCity(dto.city());
        if (dto.street() != null && !dto.street().isBlank())
            addr.setStreet(dto.street());
        if (dto.postalCode() != null && !dto.postalCode().isBlank())
            addr.setPostalCode(dto.postalCode());

        applyDefaultFlag(dto, customerId, addr);

        addr = addressWriter.save(addr, customerId, EndpointsNameMethods.ADDR_UPDATE);
        return addr;
    }

    @Transactional
    public CustomerAddress makeDefault(UUID customerId, Long id){

        CustomerAddress customerAddress = domainLookupService.getCustomerAddrOrThrow(customerId, id, EndpointsNameMethods.ADDR_MAKE_DEFAULT);

        validation.checkOwnership(customerAddress, customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT);

        // Idempotent: already default → no-op
        if(customerAddress.isDefault()){
            return customerAddress;
        }

        addressWriter.clearDefault(customerId);
        customerAddress.setDefault(true);
        customerAddress = addressWriter.save(customerAddress, customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT);

        return customerAddress;
    }

    @Transactional
    public long deleteAddress(UUID customerId, Long id){
        return addressWriter.delete(customerId, id);
    }

    // == Private Methods ==

    private void applyDefaultFlag(DTOUpdateCustomerAddress dto, UUID customerId, CustomerAddress addr) {
        if(dto.isDefault()){
            addressWriter.clearDefault(customerId);
            addr.setDefault(true);
        }
    }

    private void prepareDefaultAssignment(DTOAddCustomerAddress dto, UUID customerId){
        if(dto.isDefault()){
            addressWriter.clearDefault(customerId);
        }
    }

}
