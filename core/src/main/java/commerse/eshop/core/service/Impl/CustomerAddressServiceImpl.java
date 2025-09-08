package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.CustomerAddress;
import commerse.eshop.core.model.entity.consts.EndpointsNameMethods;
import commerse.eshop.core.model.entity.enums.AuditMessage;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.repository.CustomerAddrRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.service.AuditingService;
import commerse.eshop.core.service.CustomerAddressService;
import commerse.eshop.core.util.SortSanitizer;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import commerse.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import commerse.eshop.core.web.mapper.CustomerAddressServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class CustomerAddressServiceImpl implements CustomerAddressService {

    // == Fields ==
    private final CustomerAddrRepo customerAddrRepo;
    private final CustomerRepo customerRepo;
    private final AuditingService auditingService;
    private final SortSanitizer sortSanitizer;
    private final CustomerAddressServiceMapper customerAddressServiceMapper;

    // == Constraints - Whitelisting ==
    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "country",     "country",
            "street",      "street",
            "city",        "city",
            "postal_code", "postalCode",  // API → entity
            "created_at",  "createdAt"    // API → entity
    );

    // == Constructors ==
    @Autowired
    protected CustomerAddressServiceImpl(CustomerAddrRepo customerAddrRepo, CustomerRepo customerRepo,
                                         AuditingService auditingService, SortSanitizer sortSanitizer,
                                         CustomerAddressServiceMapper customerAddressServiceMapper){
        this.customerAddrRepo = customerAddrRepo;
        this.customerRepo = customerRepo;
        this.auditingService = auditingService;
        this.sortSanitizer = sortSanitizer;
        this.customerAddressServiceMapper = customerAddressServiceMapper;
    }

    // == Public Methods ==
    @Override
    @Transactional(readOnly = true)
    public Page<DTOCustomerAddressResponse> getAllAddresses(UUID customerId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, ALLOWED_SORTS, 25);

        Page<CustomerAddress> page = customerAddrRepo.findByCustomerCustomerId(customerId, p);

        auditingService.log(customerId, EndpointsNameMethods.ADDR_GET_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_GET_ALL_SUCCESS.getMessage());
        return page.map(customerAddressServiceMapper::toDto); // empty page is fine
    }

    @Override
    @Transactional
    public DTOCustomerAddressResponse addCustomerAddress(UUID customerId, DTOAddCustomerAddress dto) {

        Customer customerRef;

        try {
            customerRef = customerRepo.findById(customerId).orElseThrow(
                    () -> new NoSuchElementException("Customer doesn't exist")
            );
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.ADDR_ADD, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        if (dto.isDefault()) {
            int outcome = customerAddrRepo.clearDefaultsForCustomer(customerId);
            log.debug("Cleared {} previous default addresses for customerId={}", outcome, customerId);
            if (outcome == 0)
                log.info("No previous default address for customerId={}", customerId);
        }

        CustomerAddress customerAddress = new CustomerAddress(customerRef, dto.country(), dto.street(), dto.city(),
                dto.postalCode(),dto.isDefault());

        try {
            customerAddrRepo.saveAndFlush(customerAddress);
            auditingService.log(customerId, EndpointsNameMethods.ADDR_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_ADD_SUCCESS.getMessage());
            return customerAddressServiceMapper.toDto(customerAddress);
        } catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.ADDR_ADD, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional
    @Override
    public DTOCustomerAddressResponse updateCustomerAddress(UUID customerId, Long id, DTOUpdateCustomerAddress dto) {

        CustomerAddress addr;

        try {
            addr = customerAddrRepo.findById(id).orElseThrow(() -> new NoSuchElementException("The address doesn't exist."));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        if(!(addr.getCustomer().getCustomerId().equals(customerId))){
            auditingService.log(customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.ERROR,
                    "Mentioned address couldn't be found for user " + customerId);
            throw new NoSuchElementException("Mentioned address couldn't be found for user " + customerId);
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
            auditingService.log(customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_UPDATE_SUCCESS.getMessage());
            return customerAddressServiceMapper.toDto(addr);
        } catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional
    @Override
    public DTOCustomerAddressResponse makeDefaultCustomerAddress(UUID customerId, Long id) {

        CustomerAddress customerAddress;

        try {
            customerAddress = customerAddrRepo.findByAddrIdAndCustomer_CustomerId(id, customerId).orElseThrow(
                    () -> new NoSuchElementException("Customer or Address doesn't exist.")
            );
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        // Idempotent: already default → no-op
        if (Boolean.TRUE.equals(customerAddress.isDefault())) {
            auditingService.log(customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT,
                    AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_MAKE_DEFAULT_SUCCESS.getMessage());
            return customerAddressServiceMapper.toDto(customerAddress);
        }

        // == Race check ==
        try {
            customerAddrRepo.clearDefaultsForCustomer(customerId);
            customerAddress.setDefault(true);
            customerAddrRepo.saveAndFlush(customerAddress);

            auditingService.log(customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT, AuditingStatus.SUCCESSFUL,
                    AuditMessage.ADDR_MAKE_DEFAULT_SUCCESS.getMessage());

            return customerAddressServiceMapper.toDto(customerAddress);
        } catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Override
    @Transactional
    public void deleteCustomerAddress(UUID customerId, Long id) {

        try {
            long deleted = customerAddrRepo.deleteByAddrIdAndCustomer_CustomerId(id, customerId);
            customerAddrRepo.flush();
            if (deleted == 0){
                auditingService.log(customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.WARNING, "Address not found");
                log.warn("No address to delete: id={} customerId={}", id, customerId);
                return;
            }
            auditingService.log(customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_DELETE_SUCCESS.getMessage());
            log.info("Address was deleted with id = " + id + " and customer id = " + customerId);
        } catch (DataIntegrityViolationException dive){
            // If address is referenced by something, deletion can fail
            Throwable most = dive.getMostSpecificCause();
            String msg = (most.getMessage() != null && !most.getMessage().isBlank()) ? most.getMessage() : dive.toString();
            auditingService.log(customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.ERROR, msg);
            throw dive;
        }
    }
}
