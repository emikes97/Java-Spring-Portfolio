package commerse.eshop.core.application.customerTest.addons.address.validation;

import commerce.eshop.core.application.customer.addons.address.validation.AuditedAddressValidation;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuditedAddressValidationTest {

    private CentralAudit centralAudit;
    private AuditedAddressValidation auditedAddressValidation;

    @BeforeEach
    void setUp() {
        centralAudit = mock(CentralAudit.class);

        auditedAddressValidation = new AuditedAddressValidation(centralAudit);
        mockAuditReturnSame(centralAudit);
    }


    @Test
    void checkOwnership_valid(){
        UUID realId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        CustomerAddress customerAddress = mock(CustomerAddress.class);

        when(customer.getCustomerId()).thenReturn(realId);
        when(customerAddress.getCustomer()).thenReturn(customer);

        assertDoesNotThrow(
                () -> auditedAddressValidation.checkOwnership(customerAddress, realId, "EP")
        );

        verifyNoInteractions(centralAudit);
    }

    @Test
    void checkOwnership_invalid_throwsAudited() {
        UUID realId = UUID.randomUUID();
        UUID foreignId = UUID.randomUUID();

        Customer customer = mock(Customer.class);
        when(customer.getCustomerId()).thenReturn(foreignId);

        CustomerAddress addr = mock(CustomerAddress.class);
        when(addr.getCustomer()).thenReturn(customer);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> auditedAddressValidation.checkOwnership(addr, realId, "EP"));

        assertEquals("Mentioned address couldn't be found for user " + realId,
                ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(NoSuchElementException.class),
                eq(realId),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                eq("Mentioned address couldn't be found for user " + realId)
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