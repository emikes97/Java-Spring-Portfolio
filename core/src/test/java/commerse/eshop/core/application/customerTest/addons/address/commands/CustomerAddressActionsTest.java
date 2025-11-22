package commerse.eshop.core.application.customerTest.addons.address.commands;

import commerce.eshop.core.application.customer.addons.address.commands.CustomerAddressActions;
import commerce.eshop.core.application.customer.addons.address.factory.AddressFactory;
import commerce.eshop.core.application.customer.addons.address.validation.AuditedAddressValidation;
import commerce.eshop.core.application.customer.addons.address.writer.AddressWriter;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerAddressActionsTest {

    private DomainLookupService domainLookupService;
    private AuditedAddressValidation auditedAddressValidation;
    private AddressWriter addressWriter;
    private AddressFactory addressFactory;
    private CustomerAddressActions customerAddressActions;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        auditedAddressValidation = mock(AuditedAddressValidation.class);
        addressWriter = mock(AddressWriter.class);
        addressFactory = mock(AddressFactory.class);

        customerAddressActions = new CustomerAddressActions(domainLookupService, auditedAddressValidation, addressWriter, addressFactory);
    }

    @Test
    void addNewCustomerAddress_successPath() {
        UUID customerId = UUID.randomUUID();

        DTOAddCustomerAddress dto = new DTOAddCustomerAddress("GR", "Street", "Athens", "11111", false);

        Customer customer = mock(Customer.class);
        CustomerAddress created = mock(CustomerAddress.class);
        CustomerAddress saved = mock(CustomerAddress.class);

        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ADDR_ADD)).thenReturn(customer);
        when(addressFactory.handle(dto, customer)).thenReturn(created);
        when(addressWriter.save(created, customerId, EndpointsNameMethods.ADDR_ADD)).thenReturn(saved);

        CustomerAddress result = customerAddressActions.addNewCustomerAddress(customerId, dto);

        assertSame(saved, result);

        verify(addressWriter, never()).clearDefault(customerId);
        verify(addressWriter).save(created, customerId, EndpointsNameMethods.ADDR_ADD);
    }

    @Test
    void addNewCustomerAddress_default_clearsOthersBeforeSaving(){
        UUID customerId = UUID.randomUUID();
        DTOAddCustomerAddress dto = new DTOAddCustomerAddress("GR", "Street", "Athens", "11111", true);
        Customer customer = mock(Customer.class);
        CustomerAddress created = mock(CustomerAddress.class);
        CustomerAddress saved = mock(CustomerAddress.class);

        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ADDR_ADD)).thenReturn(customer);
        when(addressFactory.handle(dto, customer)).thenReturn(created);
        when(addressWriter.save(created, customerId, EndpointsNameMethods.ADDR_ADD)).thenReturn(saved);

        CustomerAddress result = customerAddressActions.addNewCustomerAddress(customerId, dto);

        assertSame(saved, result);

        InOrder inOrder = inOrder(domainLookupService, addressWriter, addressFactory);

        inOrder.verify(domainLookupService).getCustomerOrThrow(customerId, EndpointsNameMethods.ADDR_ADD);
        inOrder.verify(addressWriter).clearDefault(customerId);
        inOrder.verify(addressFactory).handle(dto, customer);
        inOrder.verify(addressWriter).save(created, customerId, EndpointsNameMethods.ADDR_ADD);
    }

    @Test
    void updateExistingAddress_withUpdatedFields_success() {
        UUID customerId = UUID.randomUUID();
        Long addrId = 10L;

        DTOUpdateCustomerAddress dto = new DTOUpdateCustomerAddress(
                "GR", "Street", "Athens", "55555", true
        );

        CustomerAddress addr = mock(CustomerAddress.class);
        CustomerAddress saved = mock(CustomerAddress.class);

        when(domainLookupService.getCustomerAddrOrThrow(customerId, addrId, EndpointsNameMethods.ADDR_UPDATE))
                .thenReturn(addr);
        when(addressWriter.save(addr, customerId, EndpointsNameMethods.ADDR_UPDATE)).thenReturn(saved);

        CustomerAddress result = customerAddressActions.updateExistingAddress(customerId, addrId, dto);

        assertSame(saved, result);

        verify(auditedAddressValidation).checkOwnership(addr, customerId, EndpointsNameMethods.ADDR_UPDATE);

        verify(addr).setCountry("GR");
        verify(addr).setStreet("Street");
        verify(addr).setCity("Athens");
        verify(addr).setPostalCode("55555");

        verify(addressWriter).clearDefault(customerId);
        verify(addr).setDefault(true);

        verify(addressWriter).save(addr, customerId, EndpointsNameMethods.ADDR_UPDATE);
    }

    @Test
    void updateExistingAddress_skipsBlankFields_andDoesNotChangeDefault(){
        UUID customerId = UUID.randomUUID();
        Long addrId = 10L;

        DTOUpdateCustomerAddress dto = new DTOUpdateCustomerAddress(
                "", "", "","", false
        );

        CustomerAddress addr = mock(CustomerAddress.class);
        CustomerAddress saved = mock(CustomerAddress.class);

        when(domainLookupService.getCustomerAddrOrThrow(customerId, addrId, EndpointsNameMethods.ADDR_UPDATE)).thenReturn(addr);
        when(addressWriter.save(addr, customerId, EndpointsNameMethods.ADDR_UPDATE)).thenReturn(saved);

        CustomerAddress result = customerAddressActions.updateExistingAddress(customerId, addrId, dto);

        assertSame(saved, result);

        verify(auditedAddressValidation).checkOwnership(addr, customerId, EndpointsNameMethods.ADDR_UPDATE);

        verify(addr, never()).setCountry(anyString());
        verify(addr, never()).setStreet(anyString());
        verify(addr, never()).setCity(anyString());
        verify(addr, never()).setPostalCode(anyString());

        verify(addressWriter, never()).clearDefault(any());
        verify(addr, never()).setDefault(anyBoolean());

        verify(addressWriter).save(addr, customerId, EndpointsNameMethods.ADDR_UPDATE);
    }

    @Test
    void makeDefault_idempotent_whenAlreadyDefault() {
        UUID customerId = UUID.randomUUID();
        long id = 15L;

        CustomerAddress address = mock(CustomerAddress.class);

        when(domainLookupService.getCustomerAddrOrThrow(customerId, id, EndpointsNameMethods.ADDR_MAKE_DEFAULT)).thenReturn(address);
        when(address.isDefault()).thenReturn(true);

        CustomerAddress result = customerAddressActions.makeDefault(customerId, id);

        assertSame(address, result);

        verify(auditedAddressValidation).checkOwnership(address, customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT);
        verify(addressWriter, never()).clearDefault(any());
        verify(addressWriter, never()).save(any(), any(), anyString());
        verify(address, never()).setDefault(anyBoolean());
    }

    @Test
    void makeDefault_setsDefault_andSaves() {
        UUID customerId = UUID.randomUUID();
        long id = 77L;

        CustomerAddress address = mock(CustomerAddress.class);
        CustomerAddress saved = mock(CustomerAddress.class);

        when(domainLookupService.getCustomerAddrOrThrow(customerId, id, EndpointsNameMethods.ADDR_MAKE_DEFAULT)).thenReturn(address);
        when(address.isDefault()).thenReturn(false);
        when(addressWriter.save(address, customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT)).thenReturn(saved);

        CustomerAddress result = customerAddressActions.makeDefault(customerId, id);

        assertSame(saved, result);

        verify(auditedAddressValidation).checkOwnership(address, customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT);

        verify(addressWriter).clearDefault(customerId);
        verify(address).setDefault(true);
        verify(addressWriter).save(address, customerId, EndpointsNameMethods.ADDR_MAKE_DEFAULT);
    }

    @Test
    void deleteAddress_delegates() {
        UUID customerId = UUID.randomUUID();
        long id = 27L;

        when(addressWriter.delete(customerId, id)).thenReturn(1L);
        long result = customerAddressActions.deleteAddress(customerId, id);

        assertSame(1L, result);
        verify(addressWriter).delete(customerId, id);
    }
}