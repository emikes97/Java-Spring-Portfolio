package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.customer.commands.CustomerServiceActions;
import commerce.eshop.core.application.customer.queries.CustomerQueries;
import commerce.eshop.core.model.entity.*;
import commerce.eshop.core.application.customer.commands.CustomerRegistration;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditMessage;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.service.CustomerService;
import commerce.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerCartItemResponse;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerOrderResponse;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerResponse;
import commerce.eshop.core.web.mapper.CustomerServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    // == Fields ==
    private final CustomerRegistration customerRegistrationHandler;
    private final CustomerQueries customerQueries;
    private final CustomerServiceActions customerServiceActions;
    private final CentralAudit centralAudit;
    private final CustomerServiceMapper customerServiceMapper;

    // == Constructors ==
    @Autowired
    public CustomerServiceImpl(CentralAudit centralAudit, CustomerServiceMapper customerServiceMapper,
                               CustomerRegistration customerRegistrationHandler,
                               CustomerQueries customerQueries, CustomerServiceActions customerServiceActions) {

        this.centralAudit = centralAudit;
        this.customerServiceMapper = customerServiceMapper;
        this.customerRegistrationHandler = customerRegistrationHandler;
        this.customerQueries = customerQueries;
        this.customerServiceActions = customerServiceActions;
    }

    // == Public Methods ==

    @Override
    public DTOCustomerResponse createUser(DTOCustomerCreateUser dto) {
        Customer customer = customerRegistrationHandler.handle(dto);
        centralAudit.info(customer.getCustomerId(), EndpointsNameMethods.CREATE_USER,
                AuditingStatus.SUCCESSFUL, AuditMessage.CREATE_USER_SUCCESS.getMessage());
        return customerServiceMapper.toDtoCustomerRes(customer);
    }

    @Override
    public DTOCustomerResponse getProfile(UUID customerId) {
        final Customer customer = customerQueries.getCustomerProfile(customerId);
        centralAudit.info(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_PROFILE_SUCCESS.getMessage());
        return customerServiceMapper.toDtoCustomerRes(customer);
    }

    @Override
    public DTOCustomerResponse getProfile(String phoneOrEmail) {
        final Customer customer = customerQueries.getCustomerProfile(phoneOrEmail);
        centralAudit.info(customer.getCustomerId(), EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_PROFILE_SUCCESS.getMessage());
        return customerServiceMapper.toDtoCustomerRes(customer);
    }

    @Override
    public Page<DTOCustomerOrderResponse> getOrders(UUID customerId, Pageable pageable) {
        final Page<Order> orders = customerQueries.getCustomerOrders(customerId, pageable);
        centralAudit.info(customerId, EndpointsNameMethods.GET_ORDERS,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_ORDERS_SUCCESS.getMessage());
        return orders.map(customerServiceMapper::toDtoCustomerOrder);
    }

    @Override
    public Page<DTOCustomerCartItemResponse> getCartItems(UUID customerId, Pageable pageable) {
        final Page<CartItem> cartItems = customerQueries.getCustomerCartItems(customerId, pageable);
        centralAudit.info(customerId, EndpointsNameMethods.GET_CART_ITEMS,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_CART_ITEMS_SUCCESS.getMessage());
        return cartItems.map(customerServiceMapper::toDtoCartItem);
    }

    @Override
    public void updateName(UUID customerId, String password, String name) {
        customerServiceActions.handleUpdateName(customerId, password, name);
        centralAudit.info(customerId, EndpointsNameMethods.UPDATE_NAME,
                AuditingStatus.SUCCESSFUL, AuditMessage.UPDATE_NAME_SUCCESS.getMessage());
    }

    @Override
    public void updateSurname(UUID customerId, String password, String lastName) {
        customerServiceActions.handleUpdateSurname(customerId, password, lastName);
        centralAudit.info(customerId, EndpointsNameMethods.UPDATE_SURNAME,
                AuditingStatus.SUCCESSFUL, AuditMessage.UPDATE_SURNAME_SUCCESS.getMessage());
    }

    @Override
    public void updateFullName(UUID customerId, String password, String name, String lastName) {
        customerServiceActions.handleUpdateFullName(customerId, password, name, lastName);
        centralAudit.info(customerId, EndpointsNameMethods.UPDATE_FULLNAME,
                AuditingStatus.SUCCESSFUL, AuditMessage.UPDATE_FULLNAME_SUCCESS.getMessage());
    }

    @Override
    public void updateUserName(UUID customerId, String password, String userName) {
        customerServiceActions.handleUpdateUserName(customerId, password, userName);
        centralAudit.info(customerId, EndpointsNameMethods.UPDATE_USERNAME,
                AuditingStatus.SUCCESSFUL, AuditMessage.UPDATE_USERNAME_SUCCESS.getMessage());
    }

    @Override
    public void updateUserPassword(UUID customerId, String currentPassword, String newPassword) {
        customerServiceActions.handleUpdateUserPassword(customerId, currentPassword, newPassword);
        centralAudit.info(customerId, EndpointsNameMethods.UPDATE_PASSWORD,
                AuditingStatus.SUCCESSFUL, AuditMessage.UPDATE_PASSWORD_SUCCESS.getMessage());
    }
}
