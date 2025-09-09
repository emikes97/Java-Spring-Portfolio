package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.repository.CustomerAddrRepo;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.service.CustomerAddressService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.sort.CustomerAddrSort;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import commerce.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import commerce.eshop.core.web.mapper.CustomerAddressServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class CustomerAddressServiceImpl implements CustomerAddressService {

    // == Fields ==
    private final CustomerAddrRepo customerAddrRepo;
    private final CustomerRepo customerRepo;
    private final CentralAudit centralAudit;
    private final SortSanitizer sortSanitizer;
    private final CustomerAddressServiceMapper customerAddressServiceMapper;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    protected CustomerAddressServiceImpl(CustomerAddrRepo customerAddrRepo, CustomerRepo customerRepo,
                                         CentralAudit centralAudit, SortSanitizer sortSanitizer,
                                         CustomerAddressServiceMapper customerAddressServiceMapper,
                                         DomainLookupService domainLookupService){
        this.customerAddrRepo = customerAddrRepo;
        this.customerRepo = customerRepo;
        this.centralAudit = centralAudit;
        this.sortSanitizer = sortSanitizer;
        this.customerAddressServiceMapper = customerAddressServiceMapper;
        this.domainLookupService = domainLookupService;
    }

    // == Public Methods ==
    @Override
    @Transactional(readOnly = true)
    public Page<DTOCustomerAddressResponse> getAllAddresses(UUID customerId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, CustomerAddrSort.ALLOWED_SORTS, CustomerAddrSort.MAX_PAGE_SIZE);

        Page<CustomerAddress> page = customerAddrRepo.findByCustomerCustomerId(customerId, p);

        centralAudit.info(customerId, EndpointsNameMethods.ADDR_GET_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_GET_ALL_SUCCESS.getMessage());
        return page.map(customerAddressServiceMapper::toDto); // empty page is fine
    }

    @Override
    @Transactional
    public DTOCustomerAddressResponse addCustomerAddress(UUID customerId, DTOAddCustomerAddress dto) {

        final Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ADDR_ADD);

        if (dto.isDefault()) {
            int outcome = customerAddrRepo.clearDefaultsForCustomer(customerId);
            log.debug("Cleared {} previous default addresses for customerId={}", outcome, customerId);
            if (outcome == 0)
                log.info("No previous default address for customerId={}", customerId);
        }

        CustomerAddress customerAddress = new CustomerAddress(customer, dto.country(), dto.street(), dto.city(),
                dto.postalCode(),dto.isDefault());

        try {
            customerAddrRepo.saveAndFlush(customerAddress);
            centralAudit.info(customerId, EndpointsNameMethods.ADDR_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_ADD_SUCCESS.getMessage());
            return customerAddressServiceMapper.toDto(customerAddress);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.ADDR_ADD, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    @Override
    public DTOCustomerAddressResponse updateCustomerAddress(UUID customerId, Long id, DTOUpdateCustomerAddress dto) {

        final CustomerAddress addr = domainLookupService.getCustomerAddrOrThrow(customerId, id, EndpointsNameMethods.ADDR_UPDATE);

        if(!(addr.getCustomer().getCustomerId().equals(customerId))){
            throw centralAudit.audit(new NoSuchElementException("Mentioned address couldn't be found for user " + customerId),
                    customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.ERROR,
                    "Mentioned address couldn't be found for user " + customerId);
        }

        // == Update fields ==
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

        // == Save & Flush ==
        try {
            customerAddrRepo.saveAndFlush(addr);
            centralAudit.info(customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_UPDATE_SUCCESS.getMessage());
            return customerAddressServiceMapper.toDto(addr);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    @Override
    public DTOCustomerAddressResponse makeDefaultCustomerAddress(UUID customerId, Long id) {

        final CustomerAddress customerAddress = domainLookupService.getCustomerAddrOrThrow(customerId, id, EndpointsNameMethods.ADDR_MAKE_DEFAULT);

        // Idempotent: already default → no-op
        if (Boolean.TRUE.equals(customerAddress.isDefault())) {
            centralAudit.info(customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT,
                    AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_MAKE_DEFAULT_SUCCESS.getMessage());
            return customerAddressServiceMapper.toDto(customerAddress);
        }

        // == Race check ==
        try {
            customerAddrRepo.clearDefaultsForCustomer(customerId);
            customerAddress.setDefault(true);
            customerAddrRepo.saveAndFlush(customerAddress);

            centralAudit.info(customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT, AuditingStatus.SUCCESSFUL,
                    AuditMessage.ADDR_MAKE_DEFAULT_SUCCESS.getMessage());

            return customerAddressServiceMapper.toDto(customerAddress);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Override
    @Transactional
    public void deleteCustomerAddress(UUID customerId, Long id) {

        try {
            long deleted = customerAddrRepo.deleteByAddrIdAndCustomer_CustomerId(id, customerId);
            customerAddrRepo.flush();
            if (deleted == 0){
                centralAudit.warn(customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.WARNING, "Address not found");
                log.warn("No address to delete: id={} customerId={}", id, customerId);
                return;
            }
            centralAudit.info(customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_DELETE_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException dive){
            // If address is referenced by something, deletion can fail
            Throwable most = dive.getMostSpecificCause();
            String msg = (most.getMessage() != null && !most.getMessage().isBlank()) ? most.getMessage() : dive.toString();
            throw centralAudit.audit(dive, customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.ERROR, msg);
        }
    }
}
