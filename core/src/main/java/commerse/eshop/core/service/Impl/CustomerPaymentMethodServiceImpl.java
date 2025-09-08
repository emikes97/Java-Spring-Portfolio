package commerse.eshop.core.service.Impl;

import commerse.eshop.core.events.PaymentMethodCreatedEvent;
import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import commerse.eshop.core.model.entity.consts.EndpointsNameMethods;
import commerse.eshop.core.model.entity.enums.AuditMessage;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.model.entity.enums.TokenStatus;
import commerse.eshop.core.repository.CustomerPaymentMethodRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.service.AuditingService;
import commerse.eshop.core.service.CustomerPaymentMethodService;
import commerse.eshop.core.util.SortSanitizer;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerse.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import commerse.eshop.core.web.mapper.CustomerPaymentMethodServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class CustomerPaymentMethodServiceImpl implements CustomerPaymentMethodService {

    // == Fields ==
    private final CustomerPaymentMethodRepo customerPaymentMethodRepo;
    private final CustomerRepo customerRepo;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditingService auditingService;
    private final SortSanitizer sortSanitizer;
    private final CustomerPaymentMethodServiceMapper customerPaymentMethodServiceMapper;

    // == Whitelist & Constraints ==

    // Allowed sort columns for customer_payment_methods pagination
    public static final Map<String, String> PAYMENT_METHOD_SORT_WHITELIST = Map.ofEntries(
            Map.entry("provider", "provider"),
            Map.entry("brand", "brand"),
            Map.entry("last_4", "last4"),
            Map.entry("year_exp", "yearExp"),
            Map.entry("month_exp", "monthExp"),
            Map.entry("is_default", "isDefault"),
            Map.entry("created_at", "createdAt")
    );

    // == Constructors ==

    @Autowired
    public CustomerPaymentMethodServiceImpl(CustomerPaymentMethodRepo customerPaymentMethodRepo, CustomerRepo customerRepo,
                                            ApplicationEventPublisher applicationEventPublisher, AuditingService auditingService,
                                            SortSanitizer sortSanitizer, CustomerPaymentMethodServiceMapper customerPaymentMethodServiceMapper){
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.customerRepo = customerRepo;
        this.applicationEventPublisher = applicationEventPublisher;
        this.auditingService = auditingService;
        this.sortSanitizer = sortSanitizer;
        this.customerPaymentMethodServiceMapper = customerPaymentMethodServiceMapper;
    }

    // == Public Methods ==

    @Transactional(readOnly = true)
    @Override
    public Page<DTOPaymentMethodResponse> getAllPaymentMethods(UUID customerId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, PAYMENT_METHOD_SORT_WHITELIST, 25);

        Page<CustomerPaymentMethod> customerPayment = customerPaymentMethodRepo.findByCustomer_CustomerId(customerId, p);

        auditingService.log(customerId, EndpointsNameMethods.PM_GET_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.PM_ADD_SUCCESS.getMessage());
        return customerPayment.map(customerPaymentMethodServiceMapper::toDto);
    }

    @Transactional
    @Override
    public DTOPaymentMethodResponse addPaymentMethod(UUID customerId, DTOAddPaymentMethod dto) {

        boolean makeDefault = Boolean.TRUE.equals(dto.isDefault());

        if (makeDefault){
            try {
                int outcome = customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId);
                log.info("Updated tables={}", outcome);
            } catch (DataIntegrityViolationException dup){
                auditingService.log(customerId, EndpointsNameMethods.PM_ADD, AuditingStatus.ERROR, dup.toString());
                throw dup;
            }
        }

        CustomerPaymentMethod customerPaymentMethod = new CustomerPaymentMethod(customerRepo.getReferenceById(customerId),
                dto.provider(), dto.brand(), dto.last4(), dto.yearExp(), dto.monthExp(), makeDefault);

        customerPaymentMethod.setTokenStatus(TokenStatus.PENDING);
        customerPaymentMethod.setProviderPaymentMethodToken(null);

        try {
            customerPaymentMethodRepo.saveAndFlush(customerPaymentMethod);
            // Publish event to start the async progress
            applicationEventPublisher.publishEvent(new PaymentMethodCreatedEvent(customerId, customerPaymentMethod.getCustomerPaymentId(),
                    customerPaymentMethod.getProvider()));
            auditingService.log(customerId, EndpointsNameMethods.PM_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.PM_ADD_SUCCESS.getMessage());
            return customerPaymentMethodServiceMapper.toDto(customerPaymentMethod);
        } catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.PM_ADD, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional
    @Override
    public DTOPaymentMethodResponse updatePaymentMethod(UUID customerId, UUID paymentMethodId, DTOUpdatePaymentMethod dto) {

        CustomerPaymentMethod paymentMethod;

        try {
            paymentMethod = customerPaymentMethodRepo.findByCustomer_CustomerIdAndCustomerPaymentId(
                    customerId, paymentMethodId).orElseThrow(
                    () -> new NoSuchElementException("The payment method doesn't exist"));
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.PM_UPDATE, AuditingStatus.ERROR, e.toString());
            throw e;
        }

        // == Update fields ==
        if(dto.provider() != null && !dto.provider().isBlank())
            paymentMethod.setProvider(dto.provider());
        if(dto.brand() != null && !dto.brand().isBlank())
            paymentMethod.setBrand(dto.brand());
        if(dto.last4() != null && !dto.last4().isBlank())
            paymentMethod.setLast4(dto.last4());
        if(dto.yearExp() != null)
            paymentMethod.setYearExp(dto.yearExp());
        if(dto.monthExp() != null)
            paymentMethod.setMonthExp(dto.monthExp());
        if (dto.isDefault() != null){
            boolean makeDefault = Boolean.TRUE.equals(dto.isDefault());

            if(makeDefault)
            {
                customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId);
                paymentMethod.setDefault(true);
            } else {
                paymentMethod.setDefault(false);
            }
        }

        // == Race check ==
        try {
            customerPaymentMethodRepo.saveAndFlush(paymentMethod);
            auditingService.log(customerId, EndpointsNameMethods.PM_UPDATE, AuditingStatus.SUCCESSFUL, AuditMessage.PM_UPDATE_SUCCESS.getMessage());
            return customerPaymentMethodServiceMapper.toDto(paymentMethod);
        } catch (DataIntegrityViolationException dup){
            auditingService.log(customerId, EndpointsNameMethods.PM_UPDATE, AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public DTOPaymentMethodResponse retrievePaymentMethod(UUID customerId, UUID paymentMethodId) {

        CustomerPaymentMethod paymentMethod;
        
        try {
            paymentMethod = customerPaymentMethodRepo.findByCustomer_CustomerIdAndCustomerPaymentId(customerId, paymentMethodId).orElseThrow(
                    () -> new NoSuchElementException("The payment method doesn't exist.")
            );
            auditingService.log(customerId, EndpointsNameMethods.PM_RETRIEVE, AuditingStatus.SUCCESSFUL, AuditMessage.PM_RETRIEVE_SUCCESS.getMessage());
            return customerPaymentMethodServiceMapper.toDto(paymentMethod);
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.PM_RETRIEVE, AuditingStatus.ERROR, e.toString());
            throw e;
        }
    }

    @Transactional
    @Override
    public void deletePaymentMethod(UUID customerId, UUID paymentId) {

        try {
            long outcome = customerPaymentMethodRepo
                    .deleteByCustomer_CustomerIdAndCustomerPaymentId(customerId, paymentId);

            if (outcome == 0) {
                // Not found case
                NoSuchElementException notFound =
                        new NoSuchElementException("Payment method not found: " + paymentId);
                auditingService.log(customerId, EndpointsNameMethods.PM_DELETE,
                        AuditingStatus.WARNING, notFound.toString());
                throw notFound;
            }

            // Success case
            auditingService.log(customerId, EndpointsNameMethods.PM_DELETE,
                    AuditingStatus.SUCCESSFUL, AuditMessage.PM_DELETE_SUCCESS.getMessage());
            log.info("Payment method {} deleted for customer {}", paymentId, customerId);

        } catch (DataIntegrityViolationException dup) {
            // Constraint violation case
            auditingService.log(customerId, EndpointsNameMethods.PM_DELETE,
                    AuditingStatus.ERROR, dup.toString());
            throw dup;
        }
    }
}
