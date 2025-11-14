package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.customer.addons.payments.commands.AddCustomerPayment;
import commerce.eshop.core.application.customer.addons.payments.commands.DeletePaymentMethod;
import commerce.eshop.core.application.customer.addons.payments.commands.UpdatePaymentInformation;
import commerce.eshop.core.application.customer.addons.payments.queries.PaymentQueries;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.service.CustomerPaymentMethodService;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerce.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import commerce.eshop.core.web.mapper.CustomerPaymentMethodServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class CustomerPaymentMethodServiceImpl implements CustomerPaymentMethodService {

    // == Fields ==
    private final PaymentQueries paymentQueries;
    private final AddCustomerPayment addCustomerPayment;
    private final UpdatePaymentInformation updatePaymentInformation;
    private final DeletePaymentMethod delPaymentMethod;
    private final CentralAudit centralAudit;
    private final CustomerPaymentMethodServiceMapper customerPaymentMethodServiceMapper;

    // == Constructors ==

    @Autowired
    public CustomerPaymentMethodServiceImpl(PaymentQueries paymentQueries, AddCustomerPayment addCustomerPayment,
                                            UpdatePaymentInformation updatePaymentInformation, DeletePaymentMethod delPaymentMethod,
                                            CentralAudit centralAudit, CustomerPaymentMethodServiceMapper customerPaymentMethodServiceMapper){

        this.paymentQueries = paymentQueries;
        this.addCustomerPayment = addCustomerPayment;
        this.updatePaymentInformation = updatePaymentInformation;
        this.delPaymentMethod = delPaymentMethod;
        this.centralAudit = centralAudit;
        this.customerPaymentMethodServiceMapper = customerPaymentMethodServiceMapper;
    }

    // == Public Methods ==

    @Override
    public Page<DTOPaymentMethodResponse> getAllPaymentMethods(UUID customerId, Pageable pageable) {
        Page<CustomerPaymentMethod> paged = paymentQueries.getPagedPaymentMethods(customerId, pageable);
        centralAudit.info(customerId, EndpointsNameMethods.PM_GET_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.PM_GET_ALL_SUCCESS.getMessage());
        return paged.map(customerPaymentMethodServiceMapper::toDto);
    }

    @Override
    public DTOPaymentMethodResponse addPaymentMethod(UUID customerId, DTOAddPaymentMethod dto) {
        CustomerPaymentMethod pm = addCustomerPayment.handle(customerId, dto);
        centralAudit.info(customerId, EndpointsNameMethods.PM_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.PM_ADD_SUCCESS.getMessage());
        return customerPaymentMethodServiceMapper.toDto(pm);
    }

    @Override
    public DTOPaymentMethodResponse updatePaymentMethod(UUID customerId, UUID paymentMethodId, DTOUpdatePaymentMethod dto) {
        final CustomerPaymentMethod paymentMethod = updatePaymentInformation.handle(customerId, paymentMethodId, dto);
        centralAudit.info(customerId, EndpointsNameMethods.PM_UPDATE, AuditingStatus.SUCCESSFUL, AuditMessage.PM_UPDATE_SUCCESS.getMessage());
        return customerPaymentMethodServiceMapper.toDto(paymentMethod);
    }

    @Override
    public DTOPaymentMethodResponse retrievePaymentMethod(UUID customerId, UUID paymentMethodId) {
        CustomerPaymentMethod pm = paymentQueries.retrievePaymentMethod(customerId, paymentMethodId);
        centralAudit.info(customerId, EndpointsNameMethods.PM_RETRIEVE, AuditingStatus.SUCCESSFUL, AuditMessage.PM_RETRIEVE_SUCCESS.getMessage());
        return customerPaymentMethodServiceMapper.toDto(pm);
    }

    @Override
    public void deletePaymentMethod(UUID customerId, UUID paymentId) {
        delPaymentMethod.handle(customerId,paymentId);
        centralAudit.info(customerId, EndpointsNameMethods.PM_DELETE,
                AuditingStatus.SUCCESSFUL, AuditMessage.PM_DELETE_SUCCESS.getMessage());
    }
}
