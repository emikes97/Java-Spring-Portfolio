package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.customer.address.commands.CustomerAddressActions;
import commerce.eshop.core.application.customer.address.queries.AddressQueries;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.service.CustomerAddressService;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import commerce.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import commerce.eshop.core.web.mapper.CustomerAddressServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CustomerAddressServiceImpl implements CustomerAddressService {

    // == Fields ==
    private final CustomerAddressActions addressActions;
    private final CentralAudit centralAudit;
    private final CustomerAddressServiceMapper customerAddressServiceMapper;
    private final AddressQueries addressQueries;

    // == Constructors ==
    @Autowired
    protected CustomerAddressServiceImpl(CustomerAddressActions addressActions,
                                         CentralAudit centralAudit, CustomerAddressServiceMapper customerAddressServiceMapper, AddressQueries addressQueries){
        this.addressActions = addressActions;
        this.centralAudit = centralAudit;
        this.customerAddressServiceMapper = customerAddressServiceMapper;
        this.addressQueries = addressQueries;
    }

    // == Public Methods ==
    @Override
    public Page<DTOCustomerAddressResponse> getAllAddresses(UUID customerId, Pageable pageable) {
        Page<CustomerAddress> page = addressQueries.returnAllAddresses(customerId, pageable);
        centralAudit.info(customerId, EndpointsNameMethods.ADDR_GET_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_GET_ALL_SUCCESS.getMessage());
        return page.map(customerAddressServiceMapper::toDto); // empty page is fine
    }

    @Override
    public DTOCustomerAddressResponse addCustomerAddress(UUID customerId, DTOAddCustomerAddress dto) {
        CustomerAddress address = addressActions.addNewCustomerAddress(customerId, dto);
        centralAudit.info(customerId, EndpointsNameMethods.ADDR_ADD, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_ADD_SUCCESS.getMessage());
        return customerAddressServiceMapper.toDto(address);
    }

    @Override
    public DTOCustomerAddressResponse updateCustomerAddress(UUID customerId, Long id, DTOUpdateCustomerAddress dto) {
        CustomerAddress address = addressActions.updateExistingAddress(customerId, id, dto);
        centralAudit.info(customerId, EndpointsNameMethods.ADDR_UPDATE, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_UPDATE_SUCCESS.getMessage());
        return customerAddressServiceMapper.toDto(address);
    }

    @Override
    public DTOCustomerAddressResponse makeDefaultCustomerAddress(UUID customerId, Long id) {
        CustomerAddress address = addressActions.makeDefault(customerId, id);
        centralAudit.info(customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT, AuditingStatus.SUCCESSFUL,
                AuditMessage.ADDR_MAKE_DEFAULT_SUCCESS.getMessage());
        return customerAddressServiceMapper.toDto(address);
    }

    @Override
    public void deleteCustomerAddress(UUID customerId, Long id) {
        long deleted = addressActions.deleteAddress(customerId, id);
        if (deleted != 0){
            centralAudit.info(customerId, EndpointsNameMethods.ADDR_DELETE, AuditingStatus.SUCCESSFUL, AuditMessage.ADDR_DELETE_SUCCESS.getMessage());
        }
    }
}
