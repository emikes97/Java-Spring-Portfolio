package commerse.eshop.core.application.customerTest.addons.address.writer;

import commerce.eshop.core.application.customer.addons.address.writer.AddressWriter;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.repository.CustomerAddrRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AddressWriterTest {

    private CustomerAddrRepo customerAddrRepo;
    private CentralAudit centralAudit;
    private AddressWriter addressWriter;

    @BeforeEach
    void setUp() {
        customerAddrRepo = mock(CustomerAddrRepo.class);
        centralAudit = mock(CentralAudit.class);

        addressWriter = new AddressWriter(customerAddrRepo, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void clearDefault() {
        UUID customerId = UUID.randomUUID();
        when(customerAddrRepo.clearDefaultsForCustomer(customerId)).thenReturn(1);

        int result = addressWriter.clearDefault(customerId);

        assertEquals(1, result);
        verify(customerAddrRepo, times(1)).clearDefaultsForCustomer(customerId);
    }

    @Test
    void save_successPath() {
        UUID customerId = UUID.randomUUID();
        CustomerAddress customerAddress = mock(CustomerAddress.class);
        CustomerAddress persistedCustomerAddress = mock(CustomerAddress.class);

        when(customerAddrRepo.saveAndFlush(customerAddress)).thenReturn(persistedCustomerAddress);

        CustomerAddress result = addressWriter.save(customerAddress, customerId, "EP");

        assertSame(persistedCustomerAddress, result);
        verify(customerAddrRepo).saveAndFlush(customerAddress);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void save_duplicate_throwsAudited(){
        UUID customerId = UUID.randomUUID();
        CustomerAddress customerAddress = mock(CustomerAddress.class);
        DataIntegrityViolationException dup = new DataIntegrityViolationException("duplicate");

        when(customerAddrRepo.saveAndFlush(customerAddress)).thenThrow(dup);
        DataIntegrityViolationException duplicate = assertThrows(DataIntegrityViolationException.class,
                () -> addressWriter.save(customerAddress, customerId, "EP"));

        assertSame(dup, duplicate);
        verify(centralAudit, times(1)).audit(
                eq(dup),
                eq(customerId),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                anyString()
        );
    }

    @Test
    void delete_success() {
        UUID customerId = UUID.randomUUID();
        long id = 10L;

        when(customerAddrRepo.deleteByAddrIdAndCustomer_CustomerId(id, customerId)).thenReturn(1L);

        long result = addressWriter.delete(customerId, id);

        verify(customerAddrRepo).deleteByAddrIdAndCustomer_CustomerId(id, customerId);
        verify(customerAddrRepo).flush();
        verifyNoInteractions(centralAudit);
    }

    @Test
    void delete_notFound_warns(){
        UUID customerId = UUID.randomUUID();
        long id = 10L;

        when(customerAddrRepo.deleteByAddrIdAndCustomer_CustomerId(id, customerId)).thenReturn(0L);

        long result = addressWriter.delete(customerId, id);

        assertEquals(0L, result);

        verify(centralAudit, times(1)).warn(
                eq(customerId),
                eq(EndpointsNameMethods.ADDR_DELETE),
                eq(AuditingStatus.WARNING),
                eq("Address not found")
        );
        verify(customerAddrRepo).flush();
    }

    @Test
    void delete_fails_throwsAudited(){
        UUID customerId = UUID.randomUUID();
        long id = 10L;

        DataIntegrityViolationException dive = new DataIntegrityViolationException("constraint failed");

        when(customerAddrRepo.deleteByAddrIdAndCustomer_CustomerId(id, customerId)).thenThrow(dive);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> addressWriter.delete(customerId, id));

        assertSame(dive, exception);

        verify(centralAudit, times(1)).audit(
                eq(dive),
                eq(customerId),
                eq(EndpointsNameMethods.ADDR_DELETE),
                eq(AuditingStatus.ERROR),
                anyString()
        );
    }

    // == Private Methods ==

    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        when(auditMock.audit(
                any(RuntimeException.class),
                any(),
                anyString(),
                any(AuditingStatus.class),
                anyString()
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}