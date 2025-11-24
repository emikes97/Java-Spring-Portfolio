package commerce.eshop.core.tests.application.customerTest.writer;

import commerce.eshop.core.application.customer.writer.CustomerWriter;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.repository.CustomerRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class CustomerWriterTest {

    // == Fields ==
    private  CentralAudit centralAudit;
    private  CustomerRepo customerRepo;
    private CustomerWriter customerWriter;

    @BeforeEach
    void setUp() {
        centralAudit = mock(CentralAudit.class);
        customerRepo = mock(CustomerRepo.class);
        customerWriter = new CustomerWriter(customerRepo, centralAudit);

        // enable “return same exception” behavior for ALL tests
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void save() {
        Customer customer = mock(Customer.class);
        Customer saved = mock(Customer.class);

        when(customerRepo.saveAndFlush(customer)).thenReturn(saved);

        Customer result = customerWriter.save(customer);

        assertSame(saved, result);
        verify(customerRepo, times(1)).saveAndFlush(customer);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void save_failed(){
        Customer customer = mock(Customer.class);
        DataIntegrityViolationException dup = new DataIntegrityViolationException("duplicate email/phone");

        when(customerRepo.saveAndFlush(customer)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> customerWriter.save(customer));

        assertSame(dup, ex);

        verifyAuditCalledOnce(
                DataIntegrityViolationException.class,
                null,
                EndpointsNameMethods.CREATE_USER,
                dup.toString()
        );
    }


    // == Private Methods
    private void verifyAuditCalledOnce(
            Class<? extends RuntimeException> exType,
            UUID customerId,
            String method,
            String code
    ) {
        verify(centralAudit, times(1)).audit(
                any(exType),
                eq(customerId),
                eq(method),
                any(AuditingStatus.class),
                eq(code)
        );
    }

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