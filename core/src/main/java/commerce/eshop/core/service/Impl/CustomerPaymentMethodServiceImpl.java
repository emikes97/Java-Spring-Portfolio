package commerce.eshop.core.service.Impl;

import commerce.eshop.core.events.PaymentMethodCreatedEvent;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.enums.TokenStatus;
import commerce.eshop.core.repository.CustomerPaymentMethodRepo;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.service.CustomerPaymentMethodService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerce.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import commerce.eshop.core.web.mapper.CustomerPaymentMethodServiceMapper;
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
    private final CentralAudit centralAudit;
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
                                            ApplicationEventPublisher applicationEventPublisher, CentralAudit centralAudit,
                                            SortSanitizer sortSanitizer, CustomerPaymentMethodServiceMapper customerPaymentMethodServiceMapper){
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.customerRepo = customerRepo;
        this.applicationEventPublisher = applicationEventPublisher;
        this.centralAudit = centralAudit;
        this.sortSanitizer = sortSanitizer;
        this.customerPaymentMethodServiceMapper = customerPaymentMethodServiceMapper;
    }

    // == Public Methods ==

    @Transactional(readOnly = true)
    @Override
    public Page<DTOPaymentMethodResponse> getAllPaymentMethods(UUID customerId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, PAYMENT_METHOD_SORT_WHITELIST, 25);
        Page<CustomerPaymentMethod> customerPayment = customerPaymentMethodRepo.findByCustomer_CustomerId(customerId, p);
        centralAudit.info(customerId, EndpointsNameMethods.PM_GET_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.PM_GET_ALL_SUCCESS.getMessage());
        return customerPayment.map(customerPaymentMethodServiceMapper::toDto);
    }

    @Transactional
    @Override
    public DTOPaymentMethodResponse addPaymentMethod(UUID customerId, DTOAddPaymentMethod dto) {

        boolean makeDefault = Boolean.TRUE.equals(dto.isDefault());

        if (makeDefault){
            try {
                int outcome = customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId);
            } catch (DataIntegrityViolationException dup){
                throw centralAudit.audit(dup, customerId, EndpointsNameMethods.PM_ADD, AuditingStatus.ERROR, dup.toString());
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
            centralAudit.info(customerId, EndpointsNameMethods.PM_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.PM_ADD_SUCCESS.getMessage());
            return customerPaymentMethodServiceMapper.toDto(customerPaymentMethod);
        } catch (DataIntegrityViolationException dup){
           throw centralAudit.audit(dup, customerId, EndpointsNameMethods.PM_ADD, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    @Override
    public DTOPaymentMethodResponse updatePaymentMethod(UUID customerId, UUID paymentMethodId, DTOUpdatePaymentMethod dto) {

        final CustomerPaymentMethod paymentMethod = getPaymentMethodOrThrow(customerId, paymentMethodId, EndpointsNameMethods.PM_UPDATE);

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
            centralAudit.info(customerId, EndpointsNameMethods.PM_UPDATE, AuditingStatus.SUCCESSFUL, AuditMessage.PM_UPDATE_SUCCESS.getMessage());
            return customerPaymentMethodServiceMapper.toDto(paymentMethod);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.PM_UPDATE, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public DTOPaymentMethodResponse retrievePaymentMethod(UUID customerId, UUID paymentMethodId) {

        CustomerPaymentMethod paymentMethod = getPaymentMethodOrThrow(customerId, paymentMethodId, EndpointsNameMethods.PM_RETRIEVE);
        centralAudit.info(customerId, EndpointsNameMethods.PM_RETRIEVE, AuditingStatus.SUCCESSFUL, AuditMessage.PM_RETRIEVE_SUCCESS.getMessage());
        return customerPaymentMethodServiceMapper.toDto(paymentMethod);
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
                throw centralAudit.audit(notFound, customerId, EndpointsNameMethods.PM_DELETE,
                        AuditingStatus.WARNING, notFound.toString());
            }

            // Success case
            centralAudit.info(customerId, EndpointsNameMethods.PM_DELETE,
                    AuditingStatus.SUCCESSFUL, AuditMessage.PM_DELETE_SUCCESS.getMessage());

        } catch (DataIntegrityViolationException dup) {
            // Constraint violation case
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.PM_DELETE,
                    AuditingStatus.ERROR, dup.toString());
        }
    }

    private CustomerPaymentMethod getPaymentMethodOrThrow(UUID customerId, UUID paymentMethodId, String method){
        try {
            final CustomerPaymentMethod paymentMethod = customerPaymentMethodRepo.findByCustomer_CustomerIdAndCustomerPaymentId(
                    customerId, paymentMethodId).orElseThrow(
                    () -> new NoSuchElementException("The payment method doesn't exist"));
            return paymentMethod;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }
}
